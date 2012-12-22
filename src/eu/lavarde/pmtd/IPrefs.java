package eu.lavarde.pmtd;

public interface IPrefs {

	/* I don't think those should be part of the interface
	public void setLang(String lang);
	public String getLang();

	public void setMaxTries(int value);
	public int getMaxTries();

	public boolean isTimerVisible();
	public void setTimerVisible(boolean value);
	*/

	public int getOperation();
	public void setOperation(int value);

	public int getRounds();
	public void setRounds(int value);

	public void setMaxValue(int value);
	public int getMaxValue();

	public void setSmallNumbersMax(boolean value);
	public boolean isSmallNumbersMax();

	public void setDecimalPlaces(int value);
	public int getDecimalPlaces();

	/** A simple Setter to set the value of the table to train; the function will not do any correction.
	 * @param value The value of the table to train (or 0 if the user wants random numbers)
	 */
	public void setTableTraining(int value);
	/** A simple Getter to provide the value of the table to train; the function will also correct the preference 
	 * if it's value is too big, wrong or less than 0.
	 * @return Zero (0) if the user doesn't want to train a multiplication/addition table, else the integer they wants to train.
	 */
	public int getTableTraining();

	public boolean isPlusCarryAllowed();
	public void setPlusCarryAllowed(boolean value);

	public boolean isMinusBorrowAllowed();
	public void setMinusBorrowAllowed(boolean value);

	public boolean isMinusNegativeAllowed();
	public void setMinusNegativeAllowed(boolean value);

	public boolean isDivideRestAllowed();
	public void setDivideRestAllowed(boolean value);

	public boolean isDivideIntegers();
	public void setDivideIntegers(boolean value);

}