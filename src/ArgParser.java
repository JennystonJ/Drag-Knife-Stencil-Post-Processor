// import java.util.HashMap;

public class ArgParser {

    public enum Type{
        PATH_IN(0),
        PATH_OUT(1);

        private final int typeIndex;

        private Type(int typeIndex) {
            this.typeIndex = typeIndex;
        }

        public int getTypeIndex() {
            return typeIndex;
        }
    }

    String[] args;

    public ArgParser(String[] args) {
        this.args = args;
    }

    public String getArg(Type argType) {
        int index = argType.getTypeIndex();

        if(index >= args.length) {
            throw new ArgDoesNotExistException();
        }

        return args[argType.getTypeIndex()];
    }

    // private String[] argDef;

    // public ArgParser(String[] argDef) {
    //     this.argDef = argDef;
    // }

    // public HashMap<String, String> parseArgs(String[] args) {
    //     HashMap<String, String> values = new HashMap<>();

    //     for(int i = 0; i < args.length; i++) {
    //         values.put(argDef[i], args[i]);
    //     }

    //     return values;
    // }
}
