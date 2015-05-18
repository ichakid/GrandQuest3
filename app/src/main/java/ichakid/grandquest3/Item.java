package ichakid.grandquest3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by User on 4/23/2015.
 */
public class Item {
    private int id = -1;
    private Bitmap image;
    private String title = "";
    private boolean container;
    private int numberOf = -1;
    private boolean targetDrop;

    public Item(Bitmap image, String title, boolean container, boolean targetDrop, int id) {
        this.image = image;
        this.title = title;
        this.container = container;
        this.targetDrop = targetDrop;
        this.id = id;
    }

    public Item(boolean container, boolean targetDrop) {
        this.container = container;
        this.targetDrop = targetDrop;
    }

    public Item(Bitmap image, int numberOf, boolean container, boolean targetDrop, int id) {
        this.image = image;
        this.numberOf = numberOf;
        this.container = container;
        this.targetDrop = targetDrop;
        this.id = id;
    }

    public Item(Bitmap image, boolean container) {
        this.image = image;
        this.container = container;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImage() {
        return image;
    }

    public Boolean getContainer(){ return container; }

    public int getNumberOf(){ return numberOf; }

    public boolean isTargetDrop() {
        return targetDrop;
    }

    public int getId() {
        return id;
    }

    public static Bitmap findBitmapById(Context context, int id){
        switch(id){
            case 0:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.honey);
            case 1:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.herbs);
            case 2:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.clay);
            case 3:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.mineral);
            case 4:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.potion);
            case 5:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.incense);
            case 6:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.gems);
            case 7:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.lifeelixir);
            case 8:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.mcrystal);
            case 9:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.pstone);
            default:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.empty);
        }
    }

    public static String findNameById(int id){
        switch(id){
            case 0:
                return "Honey";
            case 1:
                return "Herbs";
            case 2:
                return "Clay";
            case 3:
                return "Mineral";
            case 4:
                return "Potion";
            case 5:
                return "Incense";
            case 6:
                return "Gems";
            case 7:
                return "Life Elixir";
            case 8:
                return "Mana Crystal";
            case 9:
                return "Philosopher Stone";
            default:
                return "";
        }
    }
}
