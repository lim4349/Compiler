import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class X86GenListener extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>(); // 각 ctx에 해당 되는 어셈블리 코드를 저장
	
	// 변수 테이블, Key -> 함수명, Value -> 그 함수에 속하는 지역 변수 리스트
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
	
	
	// 변수에 대한 클래스
	private static class Variable{
		String name;	// 변수명
		int size;		// 변수 사이즈 (예 : Integer의 경우 4 바이트 이므로 4)
		
		public Variable(String name, int size) {
			this.name = name;
			this.size = size;
		}
	}
}
