package de.intarsys.security.smartcard.pcsc;

/**
 * The iconified PC/SC connection. The methods are mapped directly to the
 * repsective PC/SC API functions.
 * 
 * This is created by {@link IPCSCContext#connect(String, int, int)}.
 * 
 */
public interface IPCSCConnection {

	/**
	 * Start a new transaction.
	 * 
	 * @throws PCSCException
	 */
	public void beginTransaction() throws PCSCException;

	/**
	 * Send a control code.
	 * 
	 * @param controlCode
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws PCSCException
	 */
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws PCSCException;

	/**
	 * Send a control code, mapped to fit the current platform where PC/SC is
	 * running. This is needed as PC/SC control codes vary between windows and
	 * PCSC Lite.
	 * 
	 * @param controlCode
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws PCSCException
	 */
	public byte[] controlMapped(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws PCSCException;

	/**
	 * Disconnect this connection. It is not valid to use this connection from
	 * now.
	 * 
	 * @param mode
	 * @throws PCSCException
	 */
	public void disconnect(int mode) throws PCSCException;

	/**
	 * End the current transaction.
	 * 
	 * @param mode
	 * @throws PCSCException
	 */
	public void endTransaction(int mode) throws PCSCException;

	/**
	 * GEt an PC/SC attribute.
	 * 
	 * @param id
	 * @return
	 * @throws PCSCException
	 */
	public byte[] getAttrib(int id) throws PCSCException;

	/**
	 * The {@link IPCSCContext} that created the connection.
	 * 
	 * @return
	 */
	public IPCSCContext getContext();

	/**
	 * The protocol used.
	 * 
	 * @return
	 */
	public int getProtocol();

	/**
	 * The share mode used.
	 * 
	 * @return
	 */
	public int getShareMode();

	/**
	 * Dummy "no op" call. This is used as a keep alive currently only as
	 * Windows PC/SC resource manager will kill the resource after some idle
	 * time.
	 * 
	 * @throws PCSCException
	 */
	public void getStatus() throws PCSCException;

	/**
	 * Reconnect the connection.
	 * 
	 * @param shareMode
	 * @param protocol
	 * @param mode
	 * @throws PCSCException
	 */
	public void reconnect(int shareMode, int protocol, int mode)
			throws PCSCException;

	/**
	 * Send bytes to the card.
	 * 
	 * In extension to the PC/SC API we introduce a flag if the content sent to
	 * the card may be logged to some device. The default implementation can log
	 * all communication.
	 * 
	 * @param bytes
	 * @param i
	 * @param length
	 * @param receiveLength
	 * @param sensitiveContent
	 * @return
	 * @throws PCSCException
	 */
	public byte[] transmit(byte[] bytes, int i, int length, int receiveLength,
			boolean sensitiveContent) throws PCSCException;

}
