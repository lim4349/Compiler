import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;


public class X86GenListener extends MiniGoBaseListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // �� ctx�� �ش� �Ǵ� ����� �ڵ带 ����

	// ���� ���̺�, Key -> �Լ���, Value -> �� �Լ��� ���ϴ� ���� ���� ����Ʈ
	static HashMap<String, List<Variable>> var_table = new HashMap<>();
	static boolean var_table_constructed = false;			// ���� ���̺��� �ϼ�����. �� ��° walk�� ��� true�� �Ǿ���� 
	int jump = 0;
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		 
		if(var_table_constructed) {
			String decl = "";
			String program = "";
		
			// decl+ �� ���� ó��
			for(int i = 0; i < ctx.getChildCount(); i++) {
				decl += (newTexts.get(ctx.getChild(i)));
			}
		
			program += "extern printf\n";		// �ܺ��Լ� ��� ���� ��� ���� �ʿ�
			program += "global main\n\n";
			program += "section .data\n";		// data ���� ��뿡 ���� ���� �ʿ�
			// data ���� ��뿡 ���� ���� �ʿ�
			program += "section.text\n";
			program += decl;
		
			newTexts.put(ctx, program);
			System.out.println(program);
		
			try {
				make_x86_file(newTexts.get(ctx));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else {
			var_table_constructed = true;		// �ѹ� walk�� �߱� ������, ���� ���̺� �ϼ�!
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
		if(var_table_constructed) {
		newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}
	}
	
	
	// ���� ���� �κ�. �ϴ� �������� ����
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {

	}

	// Type ���� �κ�. Ư���� ������ �� ����
	@Override
	public void exitType_spec(MiniGoParser.Type_specContext ctx) {

	}

	
	
	// �Լ� �κ�
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
	
		
			if(ctx.getChildCount() == 7 ) {		// fun_decl�� ��Ģ 1
				if(var_table_constructed) {
					String func_decl = "";
				String function_name = get_function_name(ctx);
				int local_var_count = ((ArrayList<Variable>)var_table.get(function_name)).size();
			
				func_decl += (function_name + ":\n");
			
				// �Լ� ���ѷα�
				func_decl += "\tpush ebp\n";
				func_decl += "\tmov ebp, esp\n";
			
				// �Լ� ����
				func_decl += newTexts.get(ctx.compound_stmt());
				
				// �Լ� ���ʷα� 
				func_decl += "\tmov esp, ebp\n";
				func_decl += "\tpop ebp\n";
			
				if(function_name.equals("main")) {	 // ���� ���Ḧ ���� exit(0) �߰�
					func_decl += "\tmov eax, 1\n";	// �ý����� ��ȣ
					func_decl += "\tmvo ebx, 0\n";	// ���ڰ� 0 : ���� ���Ḧ �ǹ�
					func_decl += "\tint 80h\n";		// �ý��� ��
				}
				newTexts.put(ctx, func_decl);
			}else {
				String function_name = get_function_name(ctx);
				ArrayList<Variable> local_var_list = (ArrayList<Variable>) var_table.get(function_name);
				int space_of_local_variables = local_var_list.size() * 4;
				
				for(Variable variable : local_var_list) {
					variable.offset =  find_final_offset(space_of_local_variables, variable.offset);
				}
				
				for(Variable variable : local_var_list) {
					//System.out.println(variable);
				}
			}
				
		}
	}
	
	
	// �Լ� ���� ���� ���� �κ�. Ư���� ������ �� ����
	public void exitParams(MiniGoParser.ParamsContext ctx) {

	}

	
	// �Լ� ����  ���� �κ�. Ư���� ������ �� ����
	@Override
	public void exitParam(MiniGoParser.ParamContext ctx) {

	}

	@Override
	public void exitStmt(MiniGoParser.StmtContext ctx) {
		if(var_table_constructed) {
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

	}

	@Override
	public void exitExpr_stmt(MiniGoParser.Expr_stmtContext ctx) {
		if(var_table_constructed) {
			newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
		}
	}
	
	
	// ���� �� �Ҵ�
	@Override
	public void exitAssign_stmt(MiniGoParser.Assign_stmtContext ctx) {
		
			// �Լ��� �������� �Ҵ繮���� ���� ���
			if(ctx.getParent().getParent().getParent().getRuleIndex() == 4) {
				if(!var_table_constructed) {
				String function_name = get_function_name(ctx);
				ArrayList<Variable> local_var_list;
			
				// assign_stmt�� �� 2��Ģ
				if(ctx.getChildCount() == 5) {
				
					// �� ���� ���� ���ǹ��� �����ϴ� �Լ��� �������� ����Ʈ�� ���� ���� ���̺�κ���  �޾ƿ���
					if(var_table.get(function_name) == null) {	// ���� �������̺� �Լ��� ���� ���� ����Ʈ�� �߰� �Ǿ����� ���� ���
						local_var_list = new ArrayList<>();
						var_table.put(function_name, local_var_list);
					}else {
						local_var_list = (ArrayList<Variable>)var_table.get(function_name);
					}
				
					String var_name = ctx.getChild(1).toString();	// ������
					int var_value = Integer.parseInt(newTexts.get(ctx.expr(0)));	// ������								// ������
					int var_offset;								// ���� offset
				
					if(local_var_list.isEmpty()) {
						var_offset = 1;				// ù ��°�� �Ҵ�Ǵ� ������� ��
					}else {
						Variable recently_added_var = get_recently_added_variable(local_var_list);
						var_offset = recently_added_var.offset + 1;	// [�ֱٿ� �߰��� ���� + 1] ��°�� �Ҵ�Ǵ� ������� ��
					}
				
					local_var_list.add(new Variable(var_name, var_value, var_offset));
				
				}

			}else {
				String function_name = get_function_name(ctx);
				String var_name = ctx.getChild(1).toString();
				Variable variable = find_variable(function_name, var_name);
				int var_value = Integer.parseInt(newTexts.get(ctx.expr(0)));	// ������								// ��
				variable.value = var_value;
				
				String assign_stmt = "\tmov dword [ebp-0x" + Integer.toHexString(variable.offset)
				  					+ "], 0x" + Integer.toHexString(variable.value) + "\n";
				newTexts.put(ctx, assign_stmt);
				}
				
		}
		
	}
	

	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		if(var_table_constructed) {
			String compound_stmt = "";
			
			for(int i = 0; i < ctx.getChildCount(); i++) {
				String temp = newTexts.get(ctx.getChild(i));
				if(temp != null) {
					compound_stmt += temp;
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
		if(var_table_constructed) {
			ArrayList<String> jump_list = new ArrayList<String>();
			for (int i = 0; i < 2; i++) {
				jump_list.add("L" + ++jump);
			}
			String var_name = ctx.expr().getChild(0).getText();
			// System.out.println(var_table.get(var_name).get(0).offset);
			// int var_value = var_table.get(ctx.expr().getChild(1)); // ������
			int var_offset = 0; // ������ offset, �� �Ҵ�� �����ǹǷ� �ϴ� 0����
	
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
			
			//for�� �ȿ� ���뱸��
			System.out.println();
			System.out.println("\tjmp" + jump_list.get(0));
			System.out.println(jump_list.get(1) + ":");
		}
	}

	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {

	}

	
	
	// ��� �������� ����(�ݺ���, ���ǹ�, �Լ� ��� ��)
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		
			String function_name;
			// �Լ��� �������� ���Ƿ� ���� ���
			if ((function_name = get_function_name(ctx)) != null) {
				if(!var_table_constructed) {
					ArrayList<Variable> local_var_list;
		
					// �� ���� ���� ���ǹ��� �����ϴ� �Լ��� �������� ����Ʈ�� ���� ���� ���̺�κ��� �޾ƿ���
					if (var_table.get(function_name) == null) { // ���� �������̺� �Լ��� ���� ���� ����Ʈ�� �߰� �Ǿ����� ���� ���
						local_var_list = new ArrayList<>();
						var_table.put(function_name, local_var_list);
					} else {
						local_var_list = (ArrayList<Variable>) var_table.get(function_name);
					}
		
					String var_name = ctx.IDENT().toString(); // ������
					int var_value; // ������
					int var_offset = 0; // ������ offset, �� �Ҵ�� �����ǹǷ� �ϴ� 0����
		
					if (ctx.getChildCount() == 3) { // �Ϲ� ���� : local_decl �� 1��Ģ
						var_value = 4; // �������� �Ҵ� �ȵǹǷ�, 4�� �ʱ�ȭ(int Ÿ�� �̶�� ����)
					} else { // �迭 ���� : local_decl �� 2��Ģ
						var_value = Integer.parseInt(ctx.LITERAL().toString()) * 4; // �������� �Ҵ� �ȵǹǷ�, [�迭 ũ�� * 4]�� �ʱ�ȭ (�迭 int Ÿ��
																					// �̶�� ����)
					}
		
					local_var_list.add(new Variable(var_name, var_value, var_offset));
				}
		}
	}

	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {
		String function_name = get_function_name(ctx);
		
		// expr �� 2��Ģ
		if(ctx.getChildCount() == 1) {	// ��(LITERAL) �Ǵ� ����(IDENT)
			if(function_name != null) {	// �Լ��� ���ϴ� ������ ��� (������ �ƴ�)
				String expr = null;
				
				if(!ctx.LITERAL().isEmpty()) {	// ��(LITERAL)
					expr = ctx.getChild(0).toString();
				}
				
				if(ctx.IDENT() != null) {		// ���� (IDENT)
					Variable variable = find_variable(function_name, ctx.IDENT().toString());
					if(variable != null) {
						expr = Integer.toString(variable.value);	// eax������ ����ҵ�?
					}
				}
				
				newTexts.put(ctx, expr);
			}
		}
		
		if(ctx.getChildCount() == 3) {
			// �Լ��� ���ϴ� ������ ���. (������ �ƴ�)
			if(function_name != null) {
				String op = ctx.getChild(1).toString();
				
				// expr �� 7��Ģ
				if(op.equals("*") || op.equals("/") || op.equals("%")) {
					
				}
				
				// expr ��  8 ��Ģ
				if(op.equals("+") || op.equals("-")) {
					
				}
				
				// expr �� 9 ��Ģ
				if(op.equals("==") || op.equals("!=") || op.equals("<=") ||  op.equals("<") || 
						op.equals(">=") || op.equals(">") || op.equals("and") || op.equals("or")) {
					
				}
				
				 // expr �� 11��Ģ
				if(op.equals("=")) {			// x = 410�� ���� �Ҵ繮 ó��
					if(var_table_constructed) {
						Variable variable = find_variable(function_name, ctx.getChild(0).toString());
						
						if(newTexts.get(ctx.getChild(2)) != null) {		// ��� �ӽ÷�
							variable.value = Integer.parseInt(newTexts.get(ctx.getChild(2)).toString());
						}

						String expr ="\tmov dword [ebp-0x" + variable.offset
								+ "], 0x" + Integer.toHexString(variable.value) + "\n";
						newTexts.put(ctx, expr);
						
					}else {
						Variable variable = find_variable(function_name, ctx.getChild(0).toString());	// ���� �Ҵ�Ǵ� ����
						if(variable.offset == 0) {		// ó�� �Ҵ�Ǵ� ������ ���
							ArrayList<Variable> local_var_list = (ArrayList<Variable>) var_table.get(function_name);
							Variable recently_added_var = get_recently_added_variable(local_var_list);	// ���� �ֱٿ� �Ҵ�� ����
							variable.offset = recently_added_var.offset + 1;
				
						}
					}
					
				}
				
			}
		}
		
	}

	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {

	}


	
	
	// �Ű� ������ �Ѿ�� context�� fun_decl�� �ڽ��� ���, function �̸� ��ȯ
	private String get_function_name(ParserRuleContext ctx) {
		while (ctx.getRuleIndex() != 4) {
			if (ctx.getRuleIndex() == 0) {
				return null; // �Ű� ������ �Ѿ�� context�� fun_decl�� �ڽ��� �ƴ� ���
			}
			ctx = ctx.getParent();
		}
		return ctx.getChild(1).toString(); // �Լ��̸� ��ȯ
	}

	// ���� ���̺��� ��ȸ�ؼ�, ���ڷ� �ѱ� �Լ���(�Ǵ� "global"), ������ �� �´� ������ ã�� �Լ�
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

	
	// ���ڷ� �ѱ� ���� ����Ʈ�� ��ȸ�� ��, ���������� �޸� ������ �Ҵ�� ������ ��ȯ�ϴ� �Լ�
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
	
	
	// ������ �������� offset�� ��ȯ�ϴ� �Լ�, // offset�� 4�� ��� [ebp-0x4]����, offset�� 8�� ��� [ebp-0x8]���� �ǹ�
	private int find_final_offset(int space_of_local_variables, int temp_offset) {
		return space_of_local_variables - (temp_offset-1) * 4;
	}
	

	// ������ ���� Ŭ����
	private static class Variable {
		String name; // ������
		int value; // ������
		int offset; // ebp�� ������ ������ offset (�� : [ebp-4]�� ����� ��� 4
					// [ebp-8]�� ����� ��� 8)

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
