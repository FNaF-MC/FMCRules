package hu.Pdani.FMCRules.util;

import hu.Pdani.FMCRules.FMCRulesPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0
 */
public class FRPlayer {
    private RandomCollection<String> rcgood = new RandomCollection<>();
    private RandomCollection<String> rcwrong = new RandomCollection<>();
    private RandomCollection<String> itemlist = new RandomCollection<>();
    private String id;
    private String title;
    private String question;
    private List<String> finished = new ArrayList<>();
    private int success = 0;
    private Player player;
    public FRPlayer(Player player){
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void addFinished(){
        if(id != null) addFinished(id);
    }
    public void addFinished(String id){
        finished.add(id);
    }
    public List<String> getFinished() {
        return finished;
    }

    public void addSuccess(){
        success++;
    }
    public int getSuccess() {
        return success;
    }

    public void newId(String id) throws FRException {
        rcgood = new RandomCollection<>();
        rcwrong = new RandomCollection<>();
        itemlist = new RandomCollection<>();
        this.id = id;
        FileConfiguration config = FMCRulesPlugin.getPlugin().getConfig();
        question = config.getString("questions." + id + ".question", "Invalid question");
        if(config.getBoolean("quiz.answers.random",true)) {
            title = question;
        } else {
            title = config.getString("quiz.title");
        }
        List<String> goodlist = config.getStringList("questions."+id+".good");
        List<String> wronglist = config.getStringList("questions."+id+".wrong");
        List<String> items = config.getStringList("questions."+id+".itemlist");
        if(goodlist.size() <= 1)
            throw new FRException("Good answers list must contain more than 1 element in question '"+id+"'");
        if(wronglist.size() <= 1)
            throw new FRException("Wrong answers list must contain more than 1 element in question '"+id+"'");
        if(items.size() <= 1)
            throw new FRException("Item list must contain more than 1 element in question '"+id+"'");
        double goodnum = (100.00/(double)goodlist.size());
        double wrongnum = (100.00/(double)goodlist.size());
        double itemnum = (100.00/(double)items.size());
        for(String g : goodlist){
            rcgood.add(goodnum,g);
        }
        for(String w : wronglist){
            rcwrong.add(wrongnum,w);
        }
        for(String i : items){
            itemlist.add(itemnum,i.toUpperCase());
        }
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public String getId() {
        return id;
    }

    public String newGood() {
        return rcgood.next();
    }
    public String newWrong() {
        return rcwrong.next();
    }
    public String newItem(){
        return itemlist.next();
    }
}
