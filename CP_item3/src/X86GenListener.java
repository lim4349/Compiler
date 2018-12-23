import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;


public class X86GenListener extends MiniGoBaseListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // 각 ctx에 해당 되는 어셈블리 코드를 저장

	// 변수 테이블, Key -> 함수명, Value -> 그 함수에 속하는 지역 변수 리스트
	HashMap<String, List<Variable>> var_table = new HashMap<>();
	
	int jump = 0;
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		String decl = "";
		String program = "";
		
		// decl+ 에 대한 처리
		for(int i = 0; i < ctx.getChildCount(); i++) {
			decl += (newTexts.get(ctx.getChild(i)));
		}
		
		program += "extern printf\n";		// 외부함수 사용 선언 기능 구현 필요
		program += "global main\n\n";
		program += "section .data\n";		// data 영역 사용에 대한 구현 필요
		// data 영역 사용에 대한 구현 필요
		program += "section.text";
		program += decl;
		
		newTexts.put(ctx, program);
		System.out.println(program);
		
		/*
	//	System.out.println("\tsub esp, 0x" + local_var_table.get("main").size() * 4);
		System.out.println(); // 구분하기위해 넣어둠, 최종에는 제거
		
		*/
		

		try {
			make_x86_file(newTexts.get(ctx));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void make_x86_file(String code) throws IOException {
		File file = new File("test.asm");

		if (!file.exists()) {
			file.createNewFile();
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(code);
		writer.flush();
		writer.close();
	}

	
	@Override
	public void exitDecl(MiniGoParser.DeclContext ctx) {

		newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
	}
	
	
	// 전역 변수 부분. 일단 구현하지 않음
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {

	}

	// Type 관련 부분. 특별히 구현할 것 없음
	@Override
	public void exitType_spec(MiniGoParser.Type_specContext ctx) {

	}

	
	
	// 함수 부분
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
		String func_decl = "";
		
		if(ctx.getChildCount() == 7 ) {		// fun_decl의 규칙 1
			String function_name = get_function_name(ctx);
			int local_var_count = ((ArrayList<Variable>)var_table.get(function_name)).size();
			
			func_decl += (function_name + ":\n");
			
			// 함수 프롤로그
			func_decl += "\tpush ebp\n";
			func_decl += "\tmov ebp, esp\n";
			
		    // 함수 내용
			func_decl += newTexts.get(ctx.compound_stmt());
			
			// 함수 에필로그 
			func_decl += "\tmov esp, ebp\n";
			func_decl += "\tpop ebp";
			
			if(function_name.equals("main")) {	 // 정상 종료를 위해 exit(0) 추가
				func_decl += "\tmov eax, 1";	// 시스템콜 번호
				func_decl += "\tmvo ebx, 0";	// 인자가 0 : 정상 종료를 의미
				func_decl += "\tint 80h";		// 시스템 콜
			}
			
			newTexts.put(ctx, func_decl);
		}
	}
	
	
	// 함수 다중 인자 관련 부분. 특별히 구현할 것 없음
	public void exitParams(MiniGoParser.ParamsContext ctx) {

	}

	
	// 함수 인자  관련 부분. 특별히 구현할 것 없음
	@Override
	public void exitParam(MiniGoParser.ParamContext ctx) {

	}

	@Override
	public void exitStmt(MiniGoParser.StmtContext ctx) {

		if(ctx.expr_stmt() != null) {
			newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}
		
		if(ctx.compound_stmt() != null) {
			newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}
		
		if(ctx.assign_stmt() != null) {
			newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}
		
		if(ctx.for_stmt() != null) {
			newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}


	}

	@Override
	public void exitExpr_stmt(MiniGoParser.Expr_stmtContext ctx) {
		newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
	}
	
	
	// 변수 값 할당
	@Override
	public void exitAssign_stmt(MiniGoParser.Assign_stmtContext ctx) {
		String assign_stmt = "";
		
		// 함수의 지역변수 할당문으로 쓰일 경우
		if(ctx.getParent().getParent().getParent().getRuleIndex() == 4) {
			String function_name = get_function_name(ctx);
			ArrayList<Variable> local_var_list;
			
			// assign_stmt의 제 2규칙
			if(ctx.getChildCount() == 5) {
				
				// 이 지역 변수 정의문을 포함하는 함수의 지역변수 리스트를 지역 변수 테이블로부터  받아오기
				if(var_table.get(function_name) == null) {	// 지역 변수테이블에 함수에 대한 변수 리스트가 추가 되어있지 않은 경우
					local_var_list = new ArrayList<>();
					var_table.put(function_name, local_var_list);
				}else {
					local_var_list = (ArrayList<Variable>)var_table.get(function_name);
				}
				
				String var_name = ctx.IDENT().toString();	// 변수명
				//int var_value = Integer.parseInt(newTexts.get(ctx.expr(0)));	// 변수값								// 변수값
				int var_value = 10;
				int var_offset;								// 변수 offset
				
				if(local_var_list.isEmpty()) {
					var_offset = 1;				// 첫 번째로 할당되는 변수라는 뜻
				}else {
					Variable recently_added_var = get_recently_added_variable(local_var_list);
					var_offset = recently_added_var.offset + 1;	// [최근에 추가된 변수 + 1] 번째로 할당되는 변수라는 뜻
				}
				
				local_var_list.add(new Variable(var_name, var_value, var_offset));
				
				assign_stmt += var_name;			// 일단 변수명으로 추가
				newTexts.put(ctx, assign_stmt);		// compound_stmt에서  mov dword [ebp-8], 0x0 과 같은 코드로 처리함
			}

		}
		
	}
	
	// 변수값 할당 최종 처리
	// stmt* 에 대한 부분이다. local_decl* 에 대한 처리는 특별히 구현할 것 없음
	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		String compound_stmt = "";
		
		// fun_decl의 compound_stmt인 경우
		if(ctx.getParent().getRuleIndex() == 4) {
			ParserRuleContext assign_stmtContext;
			String function_name = get_function_name(ctx);
			int space_of_local_variables = get_space_of_local_variables(function_name);
			
			compound_stmt += "\tsub esp, 0x" + Integer.toHexString(space_of_local_variables) + "\n";
			
			for(int i = 0; i < ctx.getChildCount(); i++) {	
				assign_stmtContext = ctx.stmt(i).assign_stmt(); 
				if(assign_stmtContext != null && assign_stmtContext.getRuleIndex() == 9) { // assign_stmt인 경우	
					String var_name = newTexts.get(assign_stmtContext);
					Variable variable = find_variable(function_name, var_name);
					variable.offset = find_final_offset(space_of_local_variables, variable.offset); // 여기서 비로소 최종 offset이 결정됨
																									// offset이 4일 경우 [ebp-0x4]임을, offset이 8일 경우 [ebp-0x8]임을 의미
					String assign_text = "\tmov dword [ebp-0x" + Integer.toHexString(variable.offset)
										+ "], 0x" + Integer.toHexString(variable.value) + "\n";
					//newTexts.get(assign_stmtContext).replaceAll(var_name, assign_text);
					newTexts.put(assign_stmtContext, assign_text);
					newTexts.put(ctx.stmt(i), newTexts.get(assign_stmtContext));
					
					compound_stmt += assign_text;
				}
			}
			newTexts.put(ctx, compound_stmt);
		}


	}


	@Override
	public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {

	}

	@Override
	public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {

		ArrayList<String> jump_list = new ArrayList<String>();
		for (int i = 0; i < 2; i++) {
			jump_list.add("L" + ++jump);
		}
		String var_name = ctx.expr().getChild(0).getText();
		// System.out.println(var_table.get(var_name).get(0).offset);
		// int var_value = var_table.get(ctx.expr().getChild(1)); // 변수값
		int var_offset = 0; // 변수의 offset, 값 할당시 결정되므로 일단 0으로

		System.out.println(jump_list.get(0) + ":");
		System.out.println("\tcmp dword [ebp-" + "], 0x" + "");

		String expr = ctx.expr().getChild(1).getText();
		switch (expr) {
		case ">":
			System.out.print("\tjl ");
			break;
		case "<":
			System.out.print("\tjg ");
			break;
		case ">=":
			System.out.print("\tjle ");
			break;
		case "<=":
			System.out.print("\tjge ");
			break;
		case "==":
			System.out.print("\tjne ");
			break;
		case "!=":
			System.out.print("\tje ");
			break;
		default:
			break;
		}
		System.out.println(jump_list.get(1));
		
		//for문 안에 내용구현
		System.out.println();
		System.out.println("\tjmp" + jump_list.get(0));
		System.out.println(jump_list.get(1) + ":");
	}

	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {

	}

	
	
	// 블록 지역변수 선언문(반복문, 조건문, 함수 블록 등)
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		String local_decl;
		String function_name;
		// 함수의 지역변수 정의로 쓰일 경우
		if ((function_name = get_function_name(ctx)) != null) {
			ArrayList<Variable> local_var_list;

			// 이 지역 변수 정의문을 포함하는 함수의 지역변수 리스트를 지역 변수 테이블로부터 받아오기
			if (var_table.get(function_name) == null) { // 지역 변수테이블에 함수에 대한 변수 리스트가 추가 되어있지 않은 경우
				local_var_list = new ArrayList<>();
				var_table.put(function_name, local_var_list);
			} else {
				local_var_list = (ArrayList<Variable>) var_table.get(function_name);
			}

			String var_name = ctx.IDENT().toString(); // 변수명
			int var_value; // 변수값
			int var_offset = 0; // 변수의 offset, 값 할당시 결정되므로 일단 0으로

			if (ctx.getChildCount() == 3) { // 일반 변수 : local_decl 제 1규칙
				var_value = 4; // 변수값은 할당 안되므로, 4로 초기화(int 타입 이라는 정보)
			} else { // 배열 변수 : local_decl 제 2규칙
				var_value = Integer.parseInt(ctx.LITERAL().toString()) * 4; // 변수값은 할당 안되므로, [배열 크기 * 4]로 초기화 (배열 int 타입
																			// 이라는 정보)
			}

			local_var_list.add(new Variable(var_name, var_value, var_offset));

			local_decl = ""; // local_decl에 대한 x86코드 필요없음
			newTexts.put(ctx, local_decl);
		}

	}

	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {

		String function_name = get_function_name(ctx);
		
		// expr 제 1규칙
	}

	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {

	}


	
	// 인자로 받은 함수의 지역변수 공간 크기를 리턴하는 함수
	private int get_space_of_local_variables(String function_name) {
		return var_table.get(function_name).size() * 4;
	}
	



	// 매개 변수로 넘어온 context가 fun_decl의 자식일 경우, function 이름 반환
	private String get_function_name(ParserRuleContext ctx) {
		while (ctx.getRuleIndex() != 4) {
			if (ctx.getRuleIndex() == 0) {
				return null; // 매개 변수로 넘어온 context가 fun_decl의 자식이 아닌 경우
			}
			ctx = ctx.getParent();
		}
		return ctx.getChild(1).toString(); // 함수이름 반환
	}

	// 변수 테이블을 초회해서, 인자로 넘긴 함수명(또는 "global"), 변수명 에 맞는 변수를 찾는 함수
	private Variable find_variable(String function_or_global_name, String var_name) {
		ArrayList<Variable> variable_list = (ArrayList<Variable>) var_table.get(function_or_global_name);
		Variable variable = null;
		for (int i = 0; i < variable_list.size(); i++) {
			variable = variable_list.get(i);

			if (variable.name.equals(var_name)) {
				break;
			} else {
				variable = null;
			}
		}

		return variable;
	}

	
	// 인자로 넘긴 변수 리스트를 조회한 뒤, 마지막으로 메모리 공간이 할당된 변수를 반환하는 함수
	private Variable get_recently_added_variable(ArrayList<Variable> local_var_list) {
		Variable recently_added_var = null;
		
		for(Variable var : local_var_list) {
			if(recently_added_var == null) {
				recently_added_var = var;
			}else if(var.offset > recently_added_var.offset) {
				recently_added_var = var;
			}
		}
		
		return recently_added_var;
	}
	
	
	// 변수의 최종적인 offset을 반환하는 함수, // offset이 4일 경우 [ebp-0x4]임을, offset이 8일 경우 [ebp-0x8]임을 의미
	private int find_final_offset(int space_of_local_variables, int temp_offset) {
		return space_of_local_variables - (temp_offset-1) * 4;
	}
	

	// 변수에 대한 클래스
	private static class Variable {
		String name; // 변수명
		int value; // 변수값
		int offset; // ebp로 부터의 변수의 offset (예 : [ebp-4]에 저장된 경우 4
					// [ebp-8]에 저장된 경우 8)

		public Variable(String name, int value, int offset) {
			this.name = name;
			this.value = value;
			this.offset = offset;
		}

		public String toString() {
			return "name : " + this.name + ", value : " + this.value + ", offset : " + this.offset;
		}
	}
}
