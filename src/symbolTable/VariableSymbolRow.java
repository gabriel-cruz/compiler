package symbolTable;

public class VariableSymbolRow {
	
	//identificao única
	private String id;
	//nome da variavel
	private String name;
	//tipo do símbolo (const or var)
	private String type;
	//saber se foi inicializado
	private boolean initialized;
	//o escopo
	private String scope;
	//tipo do valor controlado pelo símbolo
	private String typeData;
	
	public VariableSymbolRow(String id, String name, String type, boolean initialized, String scope, String typeData) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.initialized = initialized;
		this.scope = scope;
		this.typeData = typeData;
	}
	
	public VariableSymbolRow() {}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getTypeData() {
		return typeData;
	}

	public void setTypeData(String typeData) {
		this.typeData = typeData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		VariableSymbolRow other = (VariableSymbolRow) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SymbolRow [id=" + id + ", name=" + name + ", type=" + type + ", initiliazed=" + initialized + ", scope=" + scope + ", typeData="
				+ typeData + "]";
	}

}
