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
}
