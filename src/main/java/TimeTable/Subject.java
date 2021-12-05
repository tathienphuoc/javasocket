package TimeTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Subject {
    String subjectId;
    String subjectName;
    int credits;
    List<Class> classes;

    public Subject(Subject another) {
        this.subjectId = another.subjectId;
        this.subjectName = another.subjectName;
        this.credits = another.credits;
        this.classes = another.classes;
    }

    public static List<Subject> getSubjects(JSONObject jsonObject) {
        List<Subject> subjects = new ArrayList<>();
        JSONArray jsonSubjects = jsonObject.getJSONArray("subjects");
        for (int i = 0; i < jsonSubjects.length(); i++) {
            JSONObject jsonSubject = jsonSubjects.getJSONObject(i);
            Subject subject = new Subject(jsonSubject.getString("subjectId"),
                    jsonSubject.getString("subjectName"),
                    jsonSubject.getInt("credits"),
                    Class.getClasses(jsonSubject));
            subjects.add(subject);
        }
        return subjects;
    }

    public static Subject getById(List<Subject> subjects, String subjectId) {
        for (Subject s : subjects)
            if (s.getSubjectId().equalsIgnoreCase(subjectId))
                return new Subject(s);
        return null;
    }

    public String toString() {
        return "subjectId: " + this.subjectId + "\n" +
                "subjectName: " + this.subjectName + "\n" +
                "credits: " + this.credits + "\n" +
                this.classes.toString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("subjectId", String.valueOf(this.subjectId));
        obj.put("subjectName", String.valueOf(this.subjectName));
        obj.put("credits", String.valueOf(this.credits));
        JSONArray clsArr = new JSONArray();
        for (Class s : this.classes) {
            clsArr.put(s.toJSON());
        }
        obj.put("classes", clsArr);
        return obj;
    }
}
