package symbolTable;

import java.util.HashMap;
import java.util.Map;

public class FunctionTableSymbol {
	private String id;
	private String name;
	private String type;
	private boolean isProcedure;
	
	private Map<String,String> parameters = new HashMap<String, String>();
	
	public FunctionTableSymbol() {
		
	}
	
	public FunctionTableSymbol(String id, String name, String type, boolean isProcedure) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.isProcedure = isProcedure;
	}
	
	public void addParameter(String type, String name) {
		parameters.put(name, type);
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isProcedure() {
		return isProcedure;
	}

	public void setProcedure(boolean isProcedure) {
		this.isProcedure = isProcedure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isProcedure ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionTableSymbol other = (FunctionTableSymbol) obj;
		if (isProcedure != other.isProcedure)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TableSymbol [id=" + id + ", name=" + name + ", type=" + type + ", isProcedure=" + isProcedure + "]";
	}
	
	
	
}
