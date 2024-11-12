package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

import java.util.Stack;

public class ASTListener extends ICSSBaseListener {

	private AST ast;
	private Stack<ASTNode> nodeStack;

	public ASTListener() {
		ast = new AST();
		nodeStack = new Stack<>();
	}

	public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		nodeStack.push(stylesheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = (Stylesheet) nodeStack.pop();
		ast.setRoot(stylesheet);
	}

	// Level 0: Rules for Style, Selectors, Declarations, Literals, and Property Names

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		nodeStack.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = (Stylerule) nodeStack.pop();
		nodeStack.peek().addChild(stylerule);
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		Selector selector = determineSelectorType(ctx.getText());
		nodeStack.push(selector);
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		Selector selector = (Selector) nodeStack.pop();
		nodeStack.peek().addChild(selector);
	}

	private Selector determineSelectorType(String text) {
		if (text.startsWith("#")) {
			return new IdSelector(text);
		} else if (text.startsWith(".")) {
			return new ClassSelector(text);
		} else {
			return new TagSelector(text);
		}
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		nodeStack.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) nodeStack.pop();
		nodeStack.peek().addChild(declaration);
	}

	@Override
	public void enterLiterals(ICSSParser.LiteralsContext ctx) {
		Literal literal = createLiteral(ctx.getText());
		nodeStack.peek().addChild(literal);
	}

	private Literal createLiteral(String value) {
		if (value.startsWith("#")) {
			return new ColorLiteral(value);
		} else if (value.endsWith("px")) {
			return new PixelLiteral(value);
		} else if (value.endsWith("%")) {
			return new PercentageLiteral(value);
		} else if ("TRUE".equals(value) || "FALSE".equals(value)) {
			return new BoolLiteral(value);
		} else {
			return new ScalarLiteral(value);
		}
	}

	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName propertyName = new PropertyName(ctx.getText());
		nodeStack.peek().addChild(propertyName);
	}

	// Level 1: Handling Variable Assignments and References

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		nodeStack.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) nodeStack.pop();
		nodeStack.peek().addChild(variableAssignment);
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = new VariableReference(ctx.getText());
		nodeStack.peek().addChild(variableReference);
	}

	// Level 2: Expressions and Operations

	@Override
	public void enterExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.getChildCount() == 3) {
			Operation operation = determineOperationType(ctx.getChild(1).getText());
			nodeStack.push(operation);
		}
	}

	private Operation determineOperationType(String operator) {
		switch (operator) {
			case "*":
				return new MultiplyOperation();
			case "+":
				return new AddOperation();
			default:
				return new SubtractOperation();
		}
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.PLUS() != null || ctx.MIN() != null || ctx.MUL() != null) {
			Operation operation = (Operation) nodeStack.pop();
			nodeStack.peek().addChild(operation);
		}
	}

	// Level 3: Conditional If-Else Statements

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = new IfClause();
		nodeStack.push(ifClause);
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) nodeStack.pop();
		nodeStack.peek().addChild(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = new ElseClause();
		nodeStack.push(elseClause);
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = (ElseClause) nodeStack.pop();
		nodeStack.peek().addChild(elseClause);
	}
}
