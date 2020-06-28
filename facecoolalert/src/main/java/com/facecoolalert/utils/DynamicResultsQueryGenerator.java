package com.facecoolalert.utils;

public class DynamicResultsQueryGenerator {

    // Method to generate the SQL query for counting results by subject and custom query
    public static String getCountResultsBySubject(String subjectId, String query) {
        String res="SELECT count(*) FROM RecognitionResult WHERE "+query+" subjectId = '" + subjectId+"'";
        System.out.println("query result "+res);
        return res;
    }

    // Method to generate the SQL query for retrieving results by subject, custom query, and rank
    public static String getResultsBySubject(String subjectId, String query, int rank) {
        return getResultsBySubject(subjectId,query,rank,1);
    }
    public static String getResultsBySubject(String subjectId, String query, int rank, int limit) {
        String res = "SELECT * FROM RecognitionResult WHERE "+query+" subjectId = '" + subjectId+"' order by date desc limit "+limit+" offset "+rank;
        System.out.println("query result "+res);
        return res;
    }

    // Method to generate the SQL query for counting results by custom query
    public static String getCountResultsByQuery(String query) {
        String res = "SELECT count(*) FROM RecognitionResult WHERE "+query+" 1=1";
        System.out.println("query result "+res);
        return res;
    }

    // Method to generate the SQL query for retrieving results by custom query and rank
    public static String getResultsByQuery(String query, int rank) {
        return "SELECT * FROM RecognitionResult WHERE "+query+" 1=1 order by date desc limit 1 offset " + rank;
    }

    public static String getRangeResultsByQuery(String query, int rank) {
        int ranq=rank;
//        if(ranq%10!=0)
//            ranq-=ranq%10;
        return "SELECT * FROM RecognitionResult WHERE "+query+" 1=1 order by date desc limit 10 offset " + ranq;
    }


    public static String getWatchListUpdater(String watchlist,String condition)
    {
        String res = "update Subject set watchlist='"+watchlist+"' where "+condition;
        System.out.println("query result "+res);
        return res;
    }

    public static String getRangeResultsByQuery(String query, int start, int limit) {
        return "SELECT * FROM RecognitionResult WHERE "+query+" 1=1 order by date desc limit "+limit+" offset " + start;
    }

    public static String getRangeResultUidsByQuery(String query, int start, int limit) {
        return "SELECT uid FROM RecognitionResult WHERE "+query+" 1=1 order by date desc limit "+limit+" offset " + start;
    }
}
