package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

import java.util.List;
import java.util.stream.Collectors;

public class Generator {

	public String generate(AST ast) {
		return traverse(ast.root);
	}

	private String traverse(ASTNode node) {
		StringBuilder stringBuilder = new StringBuilder();

		if (node instanceof Stylerule) {
			stringBuilder.append(generateSelector((Stylerule) node));
			stringBuilder.append(generateDeclaration((Stylerule) node));
			stringBuilder.append("}\n\n");
		}

		for (ASTNode child : node.getChildren()) {
			stringBuilder.append(traverse(child));
		}

		return stringBuilder.toString();
	}

	private String generateSelector(Stylerule stylerule) {
		List<String> selectors = stylerule.selectors.stream()
				.map(ASTNode::toString)
				.collect(Collectors.toList());

		return String.join(", ", selectors) + " {\n";
	}

	private String generateDeclaration(Stylerule stylerule) {
		StringBuilder declarations = new StringBuilder();

		for (ASTNode node : stylerule.getChildren()) {
			if (node instanceof Declaration) {
				Declaration declaration = (Declaration) node;
				declarations.append("  ")
						.append(declaration.property.name)
						.append(": ")
						.append(generateExpression(declaration.expression))
						.append(";\n");
			}
		}

		return declarations.toString();
	}

	private String generateExpression(Expression expression) {
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		} else if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		} else if (expression instanceof ScalarLiteral) {
			return String.valueOf(((ScalarLiteral) expression).value);
		} else if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		}
		return "";
	}
}
