/*
 * Each message has a chunks of data separated by splits. The headers are defined
 * in the Protocol class, and they represent different definitions for the body
 * data following it (in some cases, none).
 */
public class Protocol {
	public static final String STATUS_ACK = "stat_ack";
	public static final String STATUS_ERR = "stat_err";
	public static final String STATUS_END = "stat_end";
	
	public static final String ACT_LOGIN = "act_log";
	public static final String ACT_REGISTER = "act_reg";
	public static final String ACT_POST = "act_post";
	
	public static final String NOTIFY = "not";
	public static final String NOTIFY_STOP = "not_stop";	// For use in filling previous messages to Viewer
	
	public static final String CLIENT_READONLY = "cli_ro";
	public static final String CLIENT_READWRITE = "cli_rw";
	
//	private static final String DATA_SPLIT = ";";
	private static final String DATA_SPLIT = "" + (char)23;
	private static final int HEADER_POS = 0;

	/**
	 * The first chunk of each packet is usually the header. This method is a
	 * convenience method for retrieving just that
	 * @param line
	 * @return the first chunk of the packet
	 */
	public static String getHeader(String line) {
		return line.split(DATA_SPLIT)[HEADER_POS];
	}
	
	/**
	 * Returns "chunks" of data present within the packet, including the header.
	 * Data starts at getChunks(packet)[1] onwards.
	 * @param packet
	 * @return
	 */
	public static String[] getChunks(String packet) {
		return packet.split(DATA_SPLIT);
	}
	
	/**
	 * Creates and returns a packet with the inputed chunks ready for sending.
	 * @param chunks Chunks to be added
	 * @return final packet ready for transmission
	 */
	public static String createPacket(String... chunks) {
		StringBuffer ret = new StringBuffer();
		for(String s : chunks) {
			ret.append(s).append(DATA_SPLIT);
		}
		return ret.toString();
	}
	
//	public static String createPacket(String header) {
//		return new StringBuffer(header).append(DATA_SPLIT).toString();
//	}
//	
//	public static String createPacket(String header, String data) {
//		return new StringBuffer(header).append(DATA_SPLIT).append(data).toString();
//	}
}
