package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.linkedList.HANLinkedList;
import nl.han.ica.datastructures.linkedList.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> scopeStack;

    public void check(AST ast) {
        scopeStack = new HANLinkedList<>();
        traverseTree(ast.root);
    }

    private void traverseTree(ASTNode node) {
        if (node instanceof Stylesheet || node instanceof Stylerule || node instanceof IfClause) {
            scopeStack.addFirst(new HashMap<>());
        }

        if (node instanceof VariableAssignment) {
            checkVariableAssignment((VariableAssignment) node);
        } else if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        } else if (node instanceof IfClause) {
            checkIfClause((IfClause) node);
        } else if (node instanceof Operation) {
            checkOperation((Operation) node);
        }

        if (node instanceof VariableReference) {
            validateVariableScope((VariableReference) node);
        }

        for (ASTNode child : node.getChildren()) {
            traverseTree(child);
        }

        if (node instanceof Stylesheet || node instanceof Stylerule || node instanceof IfClause) {
            scopeStack.removeFirst();
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        if (assignment.expression != null) {
            ExpressionType type = determineExpressionType(assignment.expression);
            if (isVariableConflict(assignment.name.name, type)) {
                assignment.setError("Variable is already defined with a different type.");
            } else {
                scopeStack.getFirst().put(assignment.name.name, type);
            }
        }
    }

    private boolean isVariableConflict(String variableName, ExpressionType type) {
        for (int i = 0; i < scopeStack.getSize(); i++) {
            if (scopeStack.get(i).containsKey(variableName) && scopeStack.get(i).get(variableName) != type) {
                return true;
            }
        }
        return false;
    }

    private void checkDeclaration(Declaration declaration) {
        if (declaration.property.name.equals("width") || declaration.property.name.equals("height")) {
            checkDimensionProperty(declaration);
        } else if (declaration.property.name.equals("color") || declaration.property.name.equals("background-color")) {
            checkColorProperty(declaration);
        }
    }

    private void checkDimensionProperty(Declaration declaration) {
        ExpressionType exprType = (declaration.expression instanceof VariableReference)
                ? getVariableType((VariableReference) declaration.expression)
                : determineExpressionType(declaration.expression);

        if (exprType != ExpressionType.PIXEL && exprType != ExpressionType.PERCENTAGE) {
            declaration.setError("Expected PIXEL or PERCENTAGE, but got " + exprType);
        }
    }

    private void checkColorProperty(Declaration declaration) {
        ExpressionType exprType = declaration.expression instanceof VariableReference
                ? getVariableType((VariableReference) declaration.expression)
                : determineExpressionType(declaration.expression);

        if (exprType != ExpressionType.COLOR) {
            declaration.setError("Expected COLOR, but got " + exprType);
        }
    }

    private void checkIfClause(IfClause clause) {
        ExpressionType conditionType;
        if (clause.conditionalExpression instanceof VariableReference) {
            VariableReference variable = (VariableReference) clause.conditionalExpression;
            conditionType = getVariableType(variable);
        } else {
            conditionType = determineExpressionType(clause.conditionalExpression);
        }

        if (conditionType != ExpressionType.BOOL) {
            clause.conditionalExpression.setError("Expected BOOL for if-condition, but got " + conditionType);
        }
    }

    private void checkOperation(Operation operation) {
        ExpressionType left = determineExpressionType(operation.lhs);
        ExpressionType right = determineExpressionType(operation.rhs);

        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Operations cannot involve color types.");
            return;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (left != right) {
                operation.setError("Operands must be of the same type for addition and subtraction.");
            }
        }

        if (operation instanceof MultiplyOperation) {
            if (left != ExpressionType.SCALAR && right != ExpressionType.SCALAR) {
                operation.setError("At least one operand must be scalar for multiplication.");
            }
        }
    }

    private void validateVariableScope(VariableReference reference) {
        boolean isVariableInScope = false;
        for (int i = 0; i < scopeStack.getSize(); i++) {
            if (scopeStack.get(i).containsKey(reference.name)) {
                isVariableInScope = true;
                break;
            }
        }
        if (!isVariableInScope) {
            reference.setError("Variable '" + reference.name + "' is being used outside of its scope.");
        }
    }

    private ExpressionType getVariableType(VariableReference reference) {
        ExpressionType type = ExpressionType.UNDEFINED;
        for (int i = 0; i < scopeStack.getSize(); i++) {
            if (scopeStack.get(i).containsKey(reference.name)) {
                type = scopeStack.get(i).get(reference.name);
                break;
            }
        }
        return type;
    }

    private ExpressionType getOperationType(Operation operation) {
        ExpressionType left = determineExpressionType(operation.lhs);
        ExpressionType right = determineExpressionType(operation.rhs);

        if (left == right) {
            return left;
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType determineExpressionType(Expression expression) {
        if (expression instanceof Literal) {
            return getLiteralType((Literal) expression);
        } else if (expression instanceof Operation) {
            return getOperationType((Operation) expression);
        } else if (expression instanceof VariableReference) {
            return getVariableType((VariableReference) expression);
        } else {
            return ExpressionType.UNDEFINED;
        }
    }

    private ExpressionType getLiteralType(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (literal instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (literal instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (literal instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (literal instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }
}
