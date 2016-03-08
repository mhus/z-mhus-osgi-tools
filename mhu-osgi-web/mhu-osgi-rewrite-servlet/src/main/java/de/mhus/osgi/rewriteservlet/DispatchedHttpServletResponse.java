package de.mhus.osgi.rewriteservlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class DispatchedHttpServletResponse implements HttpServletResponse {

	private HttpServletResponse instace;
	public String getContentType() {
		return instace.getContentType();
	}

	public void setCharacterEncoding(String charset) {
		instace.setCharacterEncoding(charset);
	}

	public void addDateHeader(String name, long date) {
		instace.addDateHeader(name, date);
	}

	public void addHeader(String name, String value) {
		instace.addHeader(name, value);
	}

	public void setBufferSize(int size) {
		instace.setBufferSize(size);
	}

	public void addIntHeader(String name, int value) {
		instace.addIntHeader(name, value);
	}

	public int getBufferSize() {
		return instace.getBufferSize();
	}

	public void flushBuffer() throws IOException {
		instace.flushBuffer();
	}

	public void resetBuffer() {
		instace.resetBuffer();
	}

	public boolean isCommitted() {
		return instace.isCommitted();
	}

	public void reset() {
		instace.reset();
	}

	public void setLocale(Locale loc) {
		instace.setLocale(loc);
	}

	public Locale getLocale() {
		return instace.getLocale();
	}

	private StringWriter sw;
	private PrintWriter writer;
	private ByteArrayOutputStream os;
	private ServletOutputStream sos;

	
	public String getContent() {
		if (writer != null) {
			writer.flush();
			return sw.toString();
		}
		if (os != null) {
			try {
				sos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new String(os.toByteArray());
		}
		return null;
	}
	
	public void addCookie(Cookie arg0) {
		instace.addCookie(arg0);
	}

	public boolean containsHeader(String arg0) {
		return instace.containsHeader(arg0);
	}

	public String encodeRedirectURL(String arg0) {
		return instace.encodeRedirectURL(arg0);
	}

	public String encodeRedirectUrl(String arg0) {
		return instace.encodeRedirectUrl(arg0);
	}

	public String encodeURL(String arg0) {
		return instace.encodeURL(arg0);
	}

	public String encodeUrl(String arg0) {
		return instace.encodeUrl(arg0);
	}

	public String getCharacterEncoding() {
		return instace.getCharacterEncoding();
	}

	public ServletOutputStream getOutputStream() throws IOException {
//		return instace.getOutputStream();
		if (os == null) {
			os = new ByteArrayOutputStream();
			sos = new ServletOutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					os.write(b);
				}
			};
		}
		return sos;
	}

	public PrintWriter getWriter() throws IOException {
//		return instace.getWriter();
		if (writer == null) {
			sw = new StringWriter();
			writer = new PrintWriter(sw);
		}
		return writer;
	}

	public void sendError(int arg0, String arg1) throws IOException {
		instace.sendError(arg0, arg1);
	}

	public void sendError(int arg0) throws IOException {
		instace.sendError(arg0);
	}

	public void sendRedirect(String arg0) throws IOException {
		instace.sendRedirect(arg0);
	}

	public void setContentLength(int arg0) {
		instace.setContentLength(arg0);
	}

	public void setContentType(String arg0) {
		instace.setContentType(arg0);
	}

	public void setDateHeader(String arg0, long arg1) {
		instace.setDateHeader(arg0, arg1);
	}

	public void setHeader(String arg0, String arg1) {
		instace.setHeader(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		instace.setIntHeader(arg0, arg1);
	}

	public void setStatus(int arg0, String arg1) {
		instace.setStatus(arg0, arg1);
	}

	public void setStatus(int arg0) {
		instace.setStatus(arg0);
	}

	public DispatchedHttpServletResponse(HttpServletResponse instance) {
		this.instace = instance;
	}
}
