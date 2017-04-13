package model;

public enum Rating {
	_1(1),_2(2),_3(3),_4(4),_5(5);
	
	private int value;

	Rating(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
}
