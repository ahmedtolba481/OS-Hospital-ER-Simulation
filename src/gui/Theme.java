public final class Theme {
    public static final String BG = "#0B1220";
    public static final String PANEL = "#121E31";
    public static final String PANEL_DARK = "#0E1728";
    public static final String BORDER = "#1E2D47";
    public static final String TEXT = "#E2E8F0";
    public static final String MUTED = "#7C8DB0";
    public static final String CYAN = "#22D3EE";
    public static final String TEAL = "#2DD4BF";
    public static final String AMBER = "#FBBF24";
    public static final String RED = "#F87171";
    public static final String GREEN = "#34D399";
    public static final String PURPLE = "#A78BFA";

    public static final String[] PATIENT_COLORS = {
        "#22D3EE", "#2DD4BF", "#A78BFA", "#F472B6", "#FBBF24",
        "#60A5FA", "#34D399", "#FB923C", "#E879F9", "#94A3B8"
    };

    public static String patientColor(int id) {
        return PATIENT_COLORS[(id - 1) % PATIENT_COLORS.length];
    }

    private Theme() {}
}
