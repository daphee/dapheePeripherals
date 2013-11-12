package daphee.dapheePeripherals.helpers;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class IPEntry {
    private String ipRange;
    private String portRange = null;
    private final String _regexIp = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"+
        "([0-9]|[1-9][0-9]|1[0-9‌​]{2}|2[0-4][0-9]|25[0-5])";
    private final String regexIp = "^"+_regexIp+"$";
    private final String regexIpRange = "^"+_regexIp+"-"+_regexIp+"$";
    private final String regexPortRange = "^[\\d]{1,5}-[\\d]{1,5}$";

    public IPEntry(String entry){
        String[] arr = entry.split(":");
        ipRange = arr[0];
        if(arr.length>1)
            portRange = arr[1];
    }
    
    private int compareIp(String ip1, String ip2){
        ip1 = ip1.replace(".","");
        ip2 = ip2.replace(".","");
        BigInteger iip1 = new BigInteger(ip1.trim());
        BigInteger iip2 = new BigInteger(ip2.trim());
        return iip1.compareTo(iip2);
    }

    public boolean matches(String ip, int port){
        boolean ipMatches = false;
        boolean portMatches = false;
        

        //ipRange equals "*"
        if(ipRange.equals("*")){
            ipMatches = true;
        }
        //ipRange is a range of ip and ip is an ip
        else if(ipRange.matches(regexIpRange) && ip.matches(regexIp)){
            String[] ips = ipRange.split("-");
            //two ips are equal
            if(compareIp(ips[0],ips[1])==0){
                ipMatches = ips[0].equals(ipRange);
            }
            //only continue if first ip is less than second ip
            else if(compareIp(ips[0],ips[1])==-1){
                ipMatches = compareIp(ip,ips[0]) == 1 && compareIp(ip,ips[1]) == -1;
            }
        }
        else {
            ipMatches = Pattern.matches(ipRange,ip); //Any other regex/string
        }

        if(portRange!=null){
            // portRange equals "*"
            if(portRange.equals("*")){
                portMatches = true;
            }
            // portRange is a range
            else if(portRange.equals(regexPortRange)){
                String[] ports = portRange.split("-");
                int p1 = Integer.parseInt(ports[0]);
                int p2 = Integer.parseInt(ports[1]);
                if(p1 > 0 && p1 < 65536 && p2 > 0 && p2 < 65536){
                    portMatches = port > p1 && port < p2;
                }
            }
            else {
                portMatches = Integer.toString(port).equals(portRange);
            }
        }
        //Empty port string means every port
        else {
            portMatches = true;
        }
        return ipMatches && portMatches;
    }
}
