package TimeTable;

import Utils.JSON;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Service {
    public static List<Subject> callAPI(String subjects) throws Exception {
        String url = "http://103.6.169.208:3000/api/subjects?s=" + subjects;
        String json = null;
        try {
            json = Jsoup.connect(url).timeout(Integer.MAX_VALUE).ignoreContentType(true).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON.parseSubjects(json);
    }

    public static List<TimeTable> schedule(List<Subject> subjects) {
        List<TimeTable> timeTables = new ArrayList<TimeTable>();
        Subject firstSubject = subjects.get(0);
        for (Class c : firstSubject.getClasses()) {
            Subject subject = new Subject(firstSubject);
            c.setSubjectId(firstSubject.getSubjectId());
            subject.setClasses(new ArrayList<Class>(Arrays.asList(c)));
            TimeTable timeTable = new TimeTable();
            timeTable.setSubjects(new ArrayList<Subject>(Arrays.asList(subject)));
            timeTable.updateState();
            timeTables.add(timeTable);
        }
        subjects.remove(0);
        List<Class> plainSubject = plainSubject(subjects);
        for (Subject s : subjects)
            for (Class c : s.getClasses())
                c.setSubjectId(s.getSubjectId());
        for (Class p : plainSubject) {
            for (int i = 0; i < timeTables.size(); i++) {
                TimeTable curTimeTable = new TimeTable(timeTables.get(i));
                if (isAvailable(curTimeTable.getSubjects(), p)) {
                    TimeTable newTimeTable = new TimeTable(curTimeTable);
                    Subject s = new Subject(Subject.getById(subjects, p.getSubjectId()));
                    s.setClasses(new ArrayList<Class>(Arrays.asList(p)));
                    newTimeTable.getSubjects().add(s);
                    newTimeTable.updateState();
                    timeTables.add(newTimeTable);
                }
            }
        }
        return TimeTable.sort(timeTables);
    }

    public static boolean isAvailable(List<Subject> subjects, Class cls) {
        if (subjects == null || cls == null) return true;
        for (Subject s : subjects)
            for (Class c : s.getClasses())
                if (s.getSubjectId().equalsIgnoreCase(cls.getSubjectId())
                        || c.isConfict(cls.getSessions()))
                    return false;
        return true;

    }

    public static List<Class> plainSubject(List<Subject> subjects) {
        List<Class> clss = new ArrayList<Class>();
        for (Subject s : subjects)
            for (Class cls : s.getClasses()) {
                Class c = new Class(cls);
                c.setSubjectId(s.getSubjectId());
                clss.add(c);
            }
        return clss;
    }

    public static List<TimeTable> filter(List<TimeTable> timeTables, JSONObject json) {
        int numSuject = json.has("numSubject") ? json.getInt("numSubject") : timeTables.get(0).getSubjects().size();

        boolean morning = json.has("morning") ? json.getBoolean("morning") : false;

        boolean afternoon = json.has("afternoon") ? json.getBoolean("afternoon") : false;

        int numDaysOn = json.has("numDaysOn") ? json.getInt("numDaysOn") : 0;

        String daysOn = json.has("daysOn") ? json.getString("daysOn").replaceAll("\\s+", "") : "";

        Integer limit = json.has("limit") ? json.getInt("limit") : 0;

        boolean minNumDaysOn = json.has("minNumDaysOn") ? json.getBoolean("minNumDaysOn") : false;

        int minDaysOn = 7;

        List<TimeTable> result = new ArrayList<TimeTable>();
        for (TimeTable t : timeTables) {
            if (t.getSubjects().size() == numSuject && (numDaysOn == 0 || t.countDaysOn() == numDaysOn)) {
                String s = "";
                for (int i = 0; i < t.getDays(); i++)
                    if (t.getDaysOn()[i])
                        s += i + ",";
                if (!s.equalsIgnoreCase(""))
                    s = s.substring(0, s.length() - 1);
                if ((daysOn.equalsIgnoreCase("") || (!daysOn.equalsIgnoreCase("") && daysOn.equalsIgnoreCase(s))) && ((afternoon == false && morning == false) || (t.isAfternoon() == afternoon && t.isMorning() == morning))) {
                    result.add(t);
                    if (t.countDaysOn() < minDaysOn)
                        minDaysOn = t.countDaysOn();
                }
            }
        }

        if (minNumDaysOn) {
            int finalMinDaysOn = minDaysOn;
            result = result.stream()
                    .filter(t -> t.countDaysOn() == finalMinDaysOn).collect(Collectors.toList());
        }

        limit = Math.min(limit, result.size());
        return limit != 0 ? result.subList(0, limit) : result;
    }
}

