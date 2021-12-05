package TimeTable;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@Data
@NoArgsConstructor
public class TimeTable implements Comparable<TimeTable> {
    int days = 8, units = 13;
    boolean[][] state = new boolean[days][units];
    boolean[] daysOn = new boolean[days];
    boolean morning, afternoon;
    List<Subject> subjects = new ArrayList<Subject>();

    public TimeTable(TimeTable another) {
        this.subjects = new ArrayList<Subject>(another.subjects);
        setState(another.getState());
    }

    public void setTimeTable(boolean[][] timeTable) {
        for (int i = 0; i < days; i++)
            for (int j = 0; j < units; j++)
                this.state[i][j] = timeTable[i][j];
    }

    public void updateState() {
        for (Subject s : this.subjects)
            for (Class c : s.getClasses())
                for (int i = 0; i < c.getSessions().size(); i++) {
                    Session curSes = c.getSessions().get(i);
                    int day = curSes.getDay();
                    int start = curSes.getStart();
                    int length = curSes.getLength();
                    this.daysOn[day] = true;
                    for (int j = 0; j < length; j++) {
                        this.state[day][start + j] = true;
                        if(start + j<=5) morning=true;
                        else afternoon=true;
                    }
                }
    }

    public String toString() {
        return "numDaysOn: " + this.countDaysOn() + "\nsubjects: " + this.subjects.toString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();


        List<String> list = new ArrayList<String>();
        for(int i=0;i<8;i++)
            if(this.daysOn[i]==true)
                list.add(String.valueOf(i));
        String joined = String.join(",", list); //
        obj.put("daysOn", String.valueOf(joined));


        obj.put("numDaysOn", String.valueOf(this.countDaysOn()));
        obj.put("morning", String.valueOf(this.morning));
        obj.put("afternoon", String.valueOf(this.afternoon));
        JSONArray subjectArr = new JSONArray();
        for (Subject s : this.subjects) {
            subjectArr.put(s.toJSON());
        }
        obj.put("subjects", subjectArr);
        return obj;
    }

    public int countDaysOn() {
        int count = 0;
        for (boolean value : this.daysOn) {
            if (value) count++;
        }
        return count;
    }

    @Override
    public int compareTo(TimeTable other) {
        if (this.getSubjects() == null || other.getSubjects() == null) {
            return 0;
        }
        return (other.getSubjects().size() - other.countDaysOn()) - (this.getSubjects().size() - this.countDaysOn());
    }

    public static List<TimeTable> sort(List<TimeTable> timeTables) {
        Map<Integer, ArrayList<TimeTable>> map = new TreeMap<Integer, ArrayList<TimeTable>>(Collections.reverseOrder());
        for (TimeTable t : timeTables) {
            int key = t.getSubjects().size();
            if (map.containsKey(key))
                map.get(key).add(t);
            else
                map.put(key, new ArrayList<TimeTable>(Arrays.asList(t)));
        }

        List<TimeTable> result = new ArrayList<TimeTable>();
        for (Map.Entry<Integer, ArrayList<TimeTable>> entry : map.entrySet()) {
            List<TimeTable> t = entry.getValue();
            Collections.sort(t);
            result.addAll(t);
        }
        return result;
    }
}
