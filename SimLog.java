import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class SimLog {
	
	enum LogStreamType {
		FILE_STREAM,
		SYS_OUT_STREAM
	}
	
	private static LogStreamType streamType = LogStreamType.FILE_STREAM;
	
	private static PrintStream writer = null;
	
	public static void setStreamType(LogStreamType streamType) {
		if(streamType == LogStreamType.FILE_STREAM) {
			try {
				SimLog.writer = new PrintStream(new FileOutputStream("SimLog.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if(streamType == LogStreamType.SYS_OUT_STREAM) {
			SimLog.writer = new PrintStream(System.out);
		}
		SimLog.streamType = streamType;
	}
			
	public static void write(String text) {
		SimLog.writer.print(text);
	}
	
	public static void writeln(String text) {
		SimLog.writer.println(text);
	}
	
	public static void close() {
		if(SimLog.streamType == LogStreamType.FILE_STREAM) {
			SimLog.writer.close();
		}
	}
	
}
