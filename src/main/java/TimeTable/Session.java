package TimeTable;

import com.google.common.primitives.Booleans;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Session {

    int day;
    int start;
    int length;
    String room;
    boolean[] weeks;

    public static List<Session> getSessions(JSONObject jsonObject) {

        List<Session> sessions = new ArrayList<>();
        JSONArray jsonSessions = jsonObject.getJSONArray("sessions");

        for (int i = 0; i < jsonSessions.length(); i++) {
            JSONObject jsonSession = jsonSessions.getJSONObject(i);
            JSONArray jsonWeeks = jsonSession.getJSONArray("week");

            boolean[] weeks = new boolean[15];
            for (int j = 0; j < 15; j++)
                weeks[j] = jsonWeeks.getInt(j) == 1 ? true : false;
            Session session = new Session(jsonSession.getInt("day"),
                    jsonSession.getInt("start"),
                    jsonSession.getInt("length"),
                    jsonSession.getString("room"),
                    weeks);
            sessions.add(session);
        }

        return sessions;
    }

    public String toString() {
        return "\tday: " + this.day + "\n" +
                "\tstart: " + this.start + "\n" +
                "\tlength: " + this.length + "\n" +
                "\troom: " + this.room + "\n" +
                "\tweek: [\n" + Booleans.join(",\n", this.weeks) + "]\n";
    }

    public JSONObject toJSON() {

        JSONObject obj = new JSONObject();
        obj.put("day", this.day);
        obj.put("start", this.start);
        obj.put("length", this.length);
        obj.put("room", this.room);
        JSONArray weekArr = new JSONArray();

        for (boolean w : this.weeks) {
            weekArr.put(w);
        }

        obj.put("week", weekArr);
        return obj;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Session) {
            Session temp = (Session) obj;
            if (this.day == temp.day && this.start == temp.start && this.length == temp.length && this.room.equalsIgnoreCase(temp.room))
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.room.hashCode());
    }
}
