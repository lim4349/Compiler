
public class X86GenListener extends MiniGoBaseListener{

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
}
