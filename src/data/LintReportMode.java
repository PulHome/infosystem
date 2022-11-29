package data;

public class LintReportMode {
    private int modeNumber;
    private String modeName;
    public static final int DEFAULT_MODE = 0;
    public static final int HARD_MODE = 1;
    public static final int NIGHTMARE_MODE = 2;

    public static LintReportMode valueOf(String modeString) {
        String[] nameAndDescr = modeString.split("-", 2);
        if (nameAndDescr.length == 2) {
            if (nameAndDescr[0].trim().equalsIgnoreCase("default")) {
                return new LintReportMode(DEFAULT_MODE, nameAndDescr[1]);
            } else if (nameAndDescr[0].trim().equalsIgnoreCase("hard")) {
                return new LintReportMode(HARD_MODE, nameAndDescr[1]);
            } else if (nameAndDescr[0].trim().equalsIgnoreCase("nightmare")) {
                return new LintReportMode(NIGHTMARE_MODE, nameAndDescr[1]);
            }
        }

        return new LintReportMode(DEFAULT_MODE, "Unknown, using default");
    }

    public LintReportMode(int i, String s) {
        modeNumber = i;
        modeName = s;
    }

    public LintReportMode() {
        modeNumber = 0;
        modeName = "default";
    }

    public int getModeNumber() {
        return modeNumber;
    }

    public void setModeNumber(int modeNumber) {
        this.modeNumber = modeNumber;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    @Override
    public String toString() {
        return modeName;
    }

    public String toStringForXml() {
        return toModeStr(modeNumber) + " - " + modeName;
    }

    private String toModeStr(int modeNumber) {
        if (modeNumber == 1) return "hard";
        if (modeNumber == 2) return "nightmare";
        return "default";
    }
}
