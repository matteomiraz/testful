package dummy;

public class StateMachine {
	private int state = 0;

	public int getState() {
		return state;
	}

	public void nextChar(char c) {
		switch(state) {
		case 0:
			if(c == '[') state = 1;
			else state = -1;
			break;

		case 1:
			if(c == '(') state = 2;
			else state = -1;
			break;

		case 2:
			if(c == '{')  state = 3;
			else state = -1;
			break;

		case 3:
			if(c == '~') state = 4;
			else state = -1;
			break;

		case 4:
			if(c == 'a') state = 5;
			else state = -1;
			break;

		case 5:
			if(c == 'x') state = 6;
			else state = -1;
			break;

		case 6:
			if(c == '}') state = 7;
			else state = -1;
			break;

		case 7:
			if(c == ')') state = 8;
			else state = -1;
			break;

		case 8:
			if(c == ']') state = 9;
			else state = -1;
			break;

		default:
			state = -1;
		}
	}
	
	
	public char nextChar() {
		switch(state) {
			case 0: return '[';
			case 1: return '(';
			case 2: return '{';
			case 3: return '~';
			case 4: return 'a';
			case 5: return 'x';
			case 6: return '}';
			case 7: return ')';
			case 8: return ']';

			default: return 'E';
			}
	}
	
}