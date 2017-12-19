import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReapplyJob implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        String sql = "select * from autorenewdb";
        Main http = Main.http;
        try {
            Connection conn = http.getDBConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                int username = rs.getInt("apply_number");
                int pass = rs.getInt("pass");
                http.renewSubscription(username+"", pass+"");
                System.out.println("reapplying for user: " + username);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
