package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.linkedList.HANLinkedList;
import nl.han.ica.datastructures.linkedList.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.icss.checker.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues.addFirst(new HashMap<>());
        traverse(ast.root);
        variableValues.removeFirst();
    }

    private void traverse(ASTNode node) {
        if (node instanceof VariableAssignment) {
            handleVariableAssignment((VariableAssignment) node);
        } else if (node instanceof IfClause) {
            handleIfElseClause((IfClause) node);
        } else if (node instanceof Declaration) {
            handleDeclaration((Declaration) node);
        }

        for (ASTNode child : node.getChildren()) {
            traverse(child);
        }
    }

    private void handleVariableAssignment(VariableAssignment assignment) {
        Literal literalValue = evaluateExpression(assignment.expression);
        if (literalValue != null) {
            variableValues.getFirst().put(assignment.name.name, literalValue);
            assignment.expression = literalValue;
        }
    }

    private void handleIfElseClause(IfClause ifClause) {
        Literal conditionalValue = evaluateExpression(ifClause.conditionalExpression);
        if (conditionalValue instanceof BoolLiteral) {
            boolean condition = ((BoolLiteral) conditionalValue).value;
            if (condition) {
                for (ASTNode node : ifClause.body) {
                    traverse(node);
                    ifClause.elseClause = null;
                }
            } else if (ifClause.elseClause != null) {
                for (ASTNode node : ifClause.elseClause.body) {
                    traverse(node);
                    ifClause.elseClause = null;
                }
            } else {

            }
            ifClause.conditionalExpression = conditionalValue;
        }

    }

    private void handleDeclaration(Declaration declaration) {
        String propertyName = declaration.property.name;
        ExpressionType expectedType = initializePropertyTypeMap().get(propertyName);
        Literal actualValue = evaluateExpression(declaration.expression);

        if (expectedType != null && actualValue != null) {
            ExpressionType actualType = determineLiteralType(actualValue);
            if (actualType != expectedType) {
                declaration.setError(new SemanticError("Type mismatch for property '" +
                        propertyName + "': expected " + expectedType + " but got " + actualType).toString());
            }
        }

        declaration.expression = actualValue;
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            return evaluateVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression);
        }
        return null;
    }

    private Literal evaluateVariableReference(VariableReference varRef) {
        HashMap<String, Literal> currentScope = variableValues.getFirst();
        return currentScope.get(varRef.name);
    }

    private Literal evaluateOperation(Operation operation) {
        Literal leftValue = evaluateExpression(operation.lhs);
        Literal rightValue = evaluateExpression(operation.rhs);

        if (operation instanceof AddOperation) {
            if (leftValue instanceof PixelLiteral && rightValue instanceof PixelLiteral) {
                int result = ((PixelLiteral) leftValue).value + ((PixelLiteral) rightValue).value;
                return new PixelLiteral(result);
            } else if (leftValue instanceof ScalarLiteral && rightValue instanceof ScalarLiteral) {
                int result = ((ScalarLiteral) leftValue).value + ((ScalarLiteral) rightValue).value;
                return new ScalarLiteral(result);
            } else if (leftValue instanceof PercentageLiteral && rightValue instanceof PercentageLiteral) {
                int result = ((PercentageLiteral) leftValue).value + ((PercentageLiteral) rightValue).value;
                return new PercentageLiteral(result);
            }
        } else if (operation instanceof MultiplyOperation) {
            if (leftValue instanceof PixelLiteral && rightValue instanceof ScalarLiteral) {
                int result = ((PixelLiteral) leftValue).value * ((ScalarLiteral) rightValue).value;
                return new PixelLiteral(result);
            } else if (leftValue instanceof ScalarLiteral && rightValue instanceof PixelLiteral) {
                int result = ((ScalarLiteral) leftValue).value * ((PixelLiteral) rightValue).value;
                return new PixelLiteral(result);
            } else if (leftValue instanceof PercentageLiteral && rightValue instanceof ScalarLiteral) {
                int result = ((PercentageLiteral) leftValue).value * ((ScalarLiteral) rightValue).value / 100;
                return new PixelLiteral(result);
            }
        }
        return null;
    }

    private HashMap<String, ExpressionType> initializePropertyTypeMap() {
        HashMap<String, ExpressionType> map = new HashMap<>();
        map.put("width", ExpressionType.PIXEL);
        map.put("height", ExpressionType.PIXEL);
        map.put("color", ExpressionType.COLOR);
        map.put("background-color", ExpressionType.COLOR);
        return map;
    }

    private ExpressionType determineLiteralType(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (literal instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (literal instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        return null;
    }
}