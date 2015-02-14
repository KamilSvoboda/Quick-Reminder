package biz.svoboda.android.quickreminder;

/**
 * @author Kamil Svoboda
 * 
 */
public class Reminder {
	Long id;
	Long datetime;
	String text;
	
	public Reminder(){
		
	}
		
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the datetime
	 */
	public Long getDatetime() {
		return datetime;
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetime(Long datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

}
