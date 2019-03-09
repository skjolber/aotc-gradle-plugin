package test;

import com.fasterxml.jackson.databind.ObjectMapper;
public class My {
	
	public static final void main(String[] args) {
		new My();
	}

	public My() {
		for(int i = 0; i < 10; i++) {
			System.out.println("Hello " + new ObjectMapper());
		}
	}

}
