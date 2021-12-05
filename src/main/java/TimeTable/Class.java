package TimeTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Class {

    String subjectId;
    String group;
    List<Session> sessions;

    public Class(Class c) {
        this.group = c.group;
        this.sessions = new ArrayList<Session>(c.getSessions());
    }

    public static Class getById(List<Class> classes, String group) {
        if (classes != null)
            for (Class c : classes)
                if (c.getGroup().equalsIgnoreCase(group))
                    return new Class(c);
        return null;
    }

    public boolean isConfict(List<Session> sessions) {

        boolean[][] curState = new boolean[8][13];
        boolean[] curWeek = new boolean[15];

        for (Session s : this.getSessions()) {
            int day = s.getDay();
            int start = s.getStart();
            int length = s.getLength();
            curWeek = s.weeks;
            for (int j = 0; j < length; j++) {
                curState[day][start + j] = true;
            }
        }

        boolean[][] newState = new boolean[8][13];
        boolean[] newWeek = new boolean[15];
        for (Session s : sessions) {
            int day = s.getDay();
            int start = s.getStart();
            int length = s.getLength();
            newWeek = s.weeks;
            for (int j = 0; j < length; j++) {
                newState[day][start + j] = true;
            }
        }

        for (int w = 0; w < 15; w++)
            if (curWeek[w] && newWeek[w])
                for (int i = 0; i < 8; i++)
                    for (int j = 0; j < 13; j++)
                        if (curState[i][j] && newState[i][j])
                            return true;
        return false;
    }

    public static List<Class> getClasses(JSONObject jsonObject) {

        List<Class> classes = new ArrayList<>();
        JSONArray jsonClasses = jsonObject.getJSONArray("classes");

        for (int i = 0; i < jsonClasses.length(); i++) {
            JSONObject jsonClass = jsonClasses.getJSONObject(i);
            Class cls = new Class(null, jsonClass.getString("group"),
                    Session.getSessions(jsonClass));
            classes.add(cls);
        }

        return classes;
    }

    public JSONObject toJSON() {

        JSONObject obj = new JSONObject();
        obj.put("group", String.valueOf(this.group));
        JSONArray sesArr = new JSONArray();

        for (Session s : this.sessions) {
            sesArr.put(s.toJSON());
        }

        obj.put("sessions", sesArr);

        return obj;
    }

    public String toString() {
        return "\tsubjectId: " + this.subjectId + "\n\t" +
                "\tgroup: " + this.group + "\n\t" +
                this.sessions.toString();
    }

    @Override
    public boolean equals(Object obj) {

        String subjectId;
        String group;
        List<Session> sessions;

        if (obj instanceof Class) {
            Class temp = (Class) obj;
            if (this.subjectId.equalsIgnoreCase(temp.subjectId) && this.group.equalsIgnoreCase(temp.group) && temp.getSessions().containsAll(this.sessions))
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.subjectId.hashCode() + this.group.hashCode());
    }
}
