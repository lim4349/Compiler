import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class X86Generator {
	public static void main(String... args) {
		
		try {
			minigo_to_x86("test.go");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static void minigo_to_x86(String mgFile) throws IOException {
		MiniGoLexer lexer = new MiniGoLexer(CharStreams.fromFileName(mgFile));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new X86GenListener(), tree);	// 변수 기록 용도
		walker.walk(new X86GenListener(), tree);	// 코드 생성 용도
	}
	
}
