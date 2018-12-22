import java.util.*;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class X86GenListener extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // 각 ctx에 해당 되는 어셈블리 코드를 저장
	
	// 변수 테이블, Key -> 함수명, Value -> 그 함수에 속하는 지역 변수 리스트
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
		System.out.println(); // 구분하기위해 넣어둠, 최종에는 제거
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
	// 변수에 대한 클래스
	private static class Variable{
		String name;	// 변수명
		int size;		// 변수 사이즈 (예 : Integer의 경우 4 바이트 이므로 4)
		
		public Variable(String name, int size) {
			this.name = name;
			this.size = size;
		}
		public String toString() {
			return this.name + this.size;
		}
	}
}
