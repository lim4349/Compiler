import java.util.*;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class X86GenListener extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // �� ctx�� �ش� �Ǵ� ����� �ڵ带 ����
	
	// ���� ���̺�, Key -> �Լ���, Value -> �� �Լ��� ���ϴ� ���� ���� ����Ʈ
	HashMap<String, List<Variable>> local_var_table = new HashMap<>();
	List<Variable> list = new ArrayList<Variable>();
	
	@Override
	public void enterProgram(MiniGoParser.ProgramContext ctx) {
		System.out.println("global main");
		System.out.println("section .text");
		System.out.println("main:");
		System.out.println("\tpush ebp");
		System.out.println("\tmov ebp, esp");
	}
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		System.out.println("\tsub esp, 0x" + local_var_table.get("main").size() * 4);
		System.out.println(); // �����ϱ����� �־��, �������� ����
		System.out.println("\tmov esp, ebp");
		System.out.println("\tpop ebp");
		System.out.println("\tret");
	}
	
	@Override 
	public void enterFor_stmt(MiniGoParser.For_stmtContext ctx) {
		
	}
	@Override
	public void enterAssign_stmt(MiniGoParser.Assign_stmtContext ctx) { // var z int = 0
		if(ctx.getChild(2).getText().equals("int")) {
			list.add(new Variable(ctx.getChild(1).getText(), (list.size() + 1) * 4));
			local_var_table.put("main", list);
			
		}
		
		
	}
	@Override 
	public void enterLocal_decl(MiniGoParser.Local_declContext ctx) { // var x int
		if(ctx.getChild(2).getText().equals("int")) {
			list.add(new Variable(ctx.getChild(1).getText(), (list.size() + 1) * 4));
			local_var_table.put("main", list);
		}
		
	}
	// ������ ���� Ŭ����
	private static class Variable{
		String name;	// ������
		int size;		// ���� ������ (�� : Integer�� ��� 4 ����Ʈ �̹Ƿ� 4)
		
		public Variable(String name, int size) {
			this.name = name;
			this.size = size;
		}
		public String toString() {
			return this.name + this.size;
		}
	}
}
