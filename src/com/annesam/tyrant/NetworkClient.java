package com.annesam.tyrant;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.annesam.sdfs.Main;

public class NetworkClient {
	String server = "127.0.0.1";
	private static int baseport = 10976;
	private int port = 1978;
	
	private Socket clientSocket = null;
	// SocketChannel sChannel = null;
	private boolean closed = true;
	private ReentrantLock nlock = new ReentrantLock();
	private DataInputStream in = null;
	private BufferedOutputStream out = null;
	private String name;
	private Process p;
	private static File chunk_location = new File(Main.chunkStore);

	private static Logger log = Logger.getLogger("sdfs");

	public NetworkClient(String name) throws IOException {
		this.name = name;
		this.startTYProcess();
		int i = 0;
		while(closed) {
			try {
				i++;
				log.info("Trying to connect to server");
				
				this.connect();
				
				if(!this.closed)
					break;
				if(i>600)
					throw new IOException("DB Server failed to come up");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}catch(Exception e) {}
		}
		log.info("Connected to DB Server");
		
	}
	
	private static synchronized int getPort() {
		return 1978;
		/*
		baseport = baseport +1;
		return baseport;
		*/
	}
	
	private void startTYProcess() throws IOException {
		/*
		File f = new File(chunk_location + File.separator + "chunk-"+name + ".tch#bnum=7000000#fpow=60#opts=le");
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		String cmdLine = "ttserver -thnum 2 -port " + this.port + " " + f.getPath();
		log.info("executing " + cmdLine);
		p = Runtime.getRuntime().exec(cmdLine);
		*/
	}
	
	

	private void connect() throws IOException {
		log.info("connecting to Tyant Server " + server + " " + port);
		clientSocket = new Socket(server, port);
		clientSocket.setKeepAlive(true);
		clientSocket.setTcpNoDelay(true);
		in = new DataInputStream(clientSocket.getInputStream());
		out = new BufferedOutputStream(clientSocket.getOutputStream());
		this.closed = false;
	}

	public void put(byte[] key, byte[] value) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(new byte[2 + 4 + 4 + key.length
				+ value.length]);
		byte[] b = { (byte) 0xC8, 0x10 };
		buf.put(b);
		buf.putInt(key.length);
		buf.putInt(value.length);
		buf.put(key);
		buf.put(value);
		buf.flip();
		try {
			nlock.lock();
			out.write(buf.array());
			out.flush();
			byte result = in.readByte();
			nlock.unlock();
			if (result != 0)
				throw new IOException("Error putting value " + result + " port = " +port);
		} catch(SocketException e) {
			if (nlock.isLocked())
				nlock.unlock();
			this.close();
			try {
				this.port = getPort();
				this.startTYProcess();
				Thread.sleep(1000);
				this.connect();
				
				this.put(key, value);
			}catch(Exception e1) {
				throw new IOException("Error putting value - unable to connect to server");
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "Error putting value ", e);
			if (nlock.isLocked())
				nlock.unlock();
			throw new IOException("Error putting value");
		} finally {
			buf.clear();
			buf = null;
		}
	}

	public byte[] get(byte[] key) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(new byte[2 + 4 + key.length]);
		byte[] b = { (byte) 0xC8, 0x30 };
		buf.put(b);
		buf.putInt(key.length);
		buf.put(key);
		buf.flip();
		try {
			nlock.lock();
			out.write(buf.array());
			out.flush();
			byte result = in.readByte();
			if (result == 1 || result == -1) {
				buf.clear();
				nlock.unlock();
				return null;
			}
			if (result != 0)
				throw new IOException("Error getting value " + result + " port " +port);

			int len = in.readInt();
			byte[] rb = new byte[len];
			in.readFully(rb);
			nlock.unlock();
			return rb;
		}catch(SocketException e) {
			if (nlock.isLocked())
				nlock.unlock();
			this.close();
			try {
				this.port = getPort();
				this.startTYProcess();
				Thread.sleep(1000);
				this.connect();
				
				return this.get(key);
			}catch(Exception e1) {
				throw new IOException("Error getting value - unable to connect to server");
			}
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Error getting value ", e);
			if (nlock.isLocked())
				nlock.unlock();
			throw new IOException("Error getting value");
		}finally {
			buf.clear();
			buf = null;
		}
	}

	public int vsize(byte[] key) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(new byte[2 + 4 + key.length]);
		byte[] b = { (byte) 0xC8, 0x38 };
		buf.put(b);
		buf.putInt(key.length);
		buf.put(key);
		buf.flip();
		try {
			nlock.lock();
			out.write(buf.array());
			out.flush();
			byte result = in.readByte();
			if (result != 0) {
				buf.clear();
				nlock.unlock();
				return -1;
			}
			int len = in.readInt();
			nlock.unlock();
			return len;
		}catch(SocketException e) {
			if (nlock.isLocked())
				nlock.unlock();
			this.close();
			try {
				this.port = getPort();
				this.startTYProcess();
				Thread.sleep(1000);
				this.connect();
				
				return this.vsize(key);
			}catch(Exception e1) {
				throw new IOException("Error putting value - unable to connect to server");
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error getting value ", e);
			if (nlock.isLocked())
				nlock.unlock();
			throw new IOException("Error getting value");
		}finally {
			buf.clear();
			buf = null;
		}
	}

	public long size() throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(new byte[2]);
		byte[] b = { (byte) 0xC8, (byte) 0x81 };
		buf.put(b);
		buf.flip();
		try {
			nlock.lock();
			out.write(buf.array());
			out.flush();
			in.readByte();
			long len = in.readLong();
			nlock.unlock();
			return len;
		} catch(SocketException e) {
			if (nlock.isLocked())
				nlock.unlock();
			this.close();
			try {
				this.port = getPort();
				this.startTYProcess();
				Thread.sleep(1000);
				this.connect();
				
				return this.size();
			}catch(Exception e1) {
				throw new IOException("Error putting value - unable to connect to server");
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "Error getting value ", e);
			if (nlock.isLocked())
				nlock.unlock();
			throw new IOException("Error getting value");
		}finally {
			buf.clear();
			buf = null;
		}
	}

	public void close() {
		try {
			this.closed = true;
			nlock.lock();
			try {
				out.close();
			} catch (Exception e) {

			}
			try {
				in.close();
			} catch (Exception e) {
			}
			try {
				this.clientSocket.close();
			} catch (Exception e) {
			}
			try {
				p.destroy();
			}catch(Exception e) {}
		} catch (Exception e) {
		} finally {
			nlock.unlock();
		}
	}

	public static void main(String[] args) throws IOException {
		NetworkClient nc = new NetworkClient("bla");
		nc.put("name".getBytes(), "sam silverberg".getBytes());
		String bla = new String(nc.get("name".getBytes()));
		System.out.println("bla = " + bla);
		System.out.println("vsize = " + nc.vsize("name".getBytes()));
		System.out.println("size = " + nc.size());
	}

}
