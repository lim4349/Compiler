import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class X86GenListener extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // 각 ctx에 해당 되는 어셈블리 코드를 저장
	
	// 변수 테이블, Key -> 함수명, Value -> 그 함수에 속하는 지역 변수 리스트
	HashMap<String, List<Variable>> var_table = new HashMap<>();
	//List<Variable> list = new ArrayList<Variable>();
	
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
		
		try {
			make_x86_file("만들어진 어셈 코드");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void make_x86_file(String code) throws IOException {
		File file = new File("test.asm");
		
		if(!file.exists()) {
			file.createNewFile();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(code);
		writer.flush();
		writer.close();
	}
	
	@Override
	public void exitDecl(MiniGoParser.DeclContext ctx) {
	
	}
	
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {
	
	}
	
	@Override
	public void exitType_spec(MiniGoParser.Type_specContext ctx) {
	
	}
	
	
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
	
	}
	
	
	@Override
	public void exitParams(MiniGoParser.ParamsContext ctx) {
		
	}
	
	@Override
	public void exitParam(MiniGoParser.ParamContext ctx) {
		
	}
	
	
	@Override
	public void exitStmt(MiniGoParser.StmtContext ctx) {
		
	}
	
	
	@Override
	public void exitExpr_stmt(MiniGoParser.Expr_stmtContext ctx) {
		
	}
	
	@Override
	public void exitAssign_stmt(MiniGoParser.Assign_stmtContext ctx) {
		
	}
	
	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		
	}
	
	@Override
	public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {
		
	}
	
	@Override
	public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {
		
	}
	
	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {
		
	}
	
	@Override 
	public void enterLocal_decl(MiniGoParser.Local_declContext ctx) { // var x int
		if(ctx.getChild(2).getText().equals("int")) {
			list.add(new Variable(ctx.getChild(1).getText(), (list.size() + 1) * 4));
			local_var_table.put("main", list);
		}
		
	}
	
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		String local_decl;
		
	}
	
	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {
		
	}
	
	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {
		
	}
	
	

	@Override
	public void enterAssign_stmt(MiniGoParser.Assign_stmtContext ctx) { // var z int = 0
		if(ctx.getChild(2).getText().equals("int")) {
			list.add(new Variable(ctx.getChild(1).getText(), (list.size() + 1) * 4));
			local_var_table.put("main", list);
			System.out.println("\tmov dword [ebp-" + list.size() * 4 + 
				"], 0x" + ctx.getChild(4).getText()); // 이게 sub전에 나와야하는데 이거 해결해야할듯
		}
		
		
	}
	
	// 매개 변수로 넘어온 context가 fun_decl의 자식일 경우, function 이름 반환
	private String get_function_name(ParserRuleContext ctx) {
		while(ctx.getRuleIndex() != 4) {
			if(ctx.getRuleIndex() == 0) {
				return null;	//매개 변수로 넘어온 context가 fun_decl의 자식이 아닌 경우
			}
			ctx = ctx.getParent();
		}
		return ctx.getChild(1).toString();	// 함수이름 반환
	}
	
	
	// 변수 테이블을 초회해서, 인자로 넘긴 함수명(또는 "global"), 변수명 에 맞는 변수를 찾는 함수
	private Variable find_variable(String function_or_global_name, String var_name) {
		ArrayList<Variable> variable_list = (ArrayList<Variable>) var_table.get(function_or_global_name);
		Variable variable = null;
		for(int i = 0; i < variable_list.size(); i++) {
			variable = variable_list.get(i);
			
			if(variable.name.equals(var_name)) {
				break;
			}else {
				variable = null;
			}
		}
		
		return variable;
	}
	
	// 변수에 대한 클래스
	private static class Variable{
		String name;	// 변수명
		int value;		// 변수값
		int offset;		// ebp로 부터의 변수의 offset (예 : [ebp-4]에 저장된 경우 4
						//							[ebp-8]에 저장된 경우 8)
		
		public Variable(String name, int value, int offset) {
			this.name = name;
			this.value = value;
			this.offset = offset;
		}
		public String toString() {
			return "name : " + this.name +", value : " + this.value +", offset : " + this.offset;
		}
	}
}
