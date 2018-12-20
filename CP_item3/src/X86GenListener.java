import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class X86GenListener extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // �� ctx�� �ش� �Ǵ� ����� �ڵ带 ����
	
	// ���� ���̺�, Key -> �Լ���, Value -> �� �Լ��� ���ϴ� ���� ���� ����Ʈ
	HashMap<String, List<Variable>> local_var_table = new HashMap<>();
	
	
	@Override
	public void enterProgram(MiniGoParser.ProgramContext ctx) {
		System.out.println("push ebp");
		System.out.println("mov ebp, esp");
		System.out.println("push ecx");
	}
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		System.out.println();
		System.out.println("mov esp, ebp");
		System.out.println("pop ebp");
		System.out.println("ret");
	}
	
	
	// ������ ���� Ŭ����
	private static class Variable{
		String name;	// ������
		int size;		// ���� ������ (�� : Integer�� ��� 4 ����Ʈ �̹Ƿ� 4)
		
		public Variable(String name, int size) {
			this.name = name;
			this.size = size;
		}
	}
}
