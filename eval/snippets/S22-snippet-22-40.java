class Snippet22 {
    static int calcInvTot(int ip, int sh, int discOrMaybeFee) {
        int s = ip + sh;
        int t = s - discOrMaybeFee;
        return Math.max(0, t);
    }
}
