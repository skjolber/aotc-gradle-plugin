package test;

import com.fasterxml.jackson.databind.ObjectMapper;
public class My {
	
	public static final void main(String[] args) {
		new My();
	}

	public My() {
		for(int i = 0; i < 10; i++) {
			if(new ObjectMapper() == null) {
				throw new RuntimeException();
			}
		}
	}
	
	public void methodNotCalled() {
		System.out.println("Not called");
	}
	
	public void staticMethodNotCalled() {
		System.out.println("Not called");
	}

}
