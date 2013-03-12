package io.trigger.forge.android.shortvideo.net;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class CustomMultipartEntity extends MultipartEntity {
	private final ProgressListener mListener;
	
	public CustomMultipartEntity(final ProgressListener listener){
		super();
		mListener = listener;
	}
	
	public CustomMultipartEntity(HttpMultipartMode mode, ProgressListener listener){
		super(mode);
		mListener = listener;
	}
	
	public CustomMultipartEntity(HttpMultipartMode mode, String boundary,Charset charset, ProgressListener listener){
		super(mode,boundary,charset);
		mListener = listener;
	}
	
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.mListener));
	}
	
	public static class CountingOutputStream extends FilterOutputStream{
		private final ProgressListener listener;
		private long transferred;
 
		public CountingOutputStream(OutputStream outstream,
				ProgressListener mListener) {
			super(outstream);
			this.listener = mListener;
			transferred = 0;
		}
		
		@Override
		public void write(byte[] buffer, int offset, int length)
				throws IOException {			
			out.write(buffer,offset,length);
			transferred += length;
			listener.transferred(transferred);
		}
	}
	public interface ProgressListener{
		public abstract void transferred(long transferred);
	}
}