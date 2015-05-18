package ichakid.grandquest3;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Sqrchara {
    private int id = -1;
    private Bitmap image;
    private String title = "";
    private boolean container;
    private boolean targetDrop;

    public Sqrchara(Bitmap image, String title, boolean container, boolean targetDrop, int id) {
        this.image = image;
        this.title = title;
        this.container = container;
        this.targetDrop = targetDrop;
        this.id = id;
    }

    public Sqrchara(boolean container, boolean targetDrop) {
        this.container = container;
        this.targetDrop = targetDrop;
    }

    public Sqrchara(Bitmap image, boolean container) {
        this.image = image;
        this.container = container;
    }

    public Sqrchara(boolean container){
        this.container = container;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImage() {
        return image;
    }

    public Boolean getContainer(){ return container; }

    public boolean isTargetDrop() {
        return targetDrop;
    }

    public int getId() {
        return id;
    }

    public static Bitmap findBitmapById(Context context, int id){
        switch(id){
            case 0:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.pooh);
            case 1:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.tilegg);
            case 2:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.pooh_shadow);
            default:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.tilegg);
        }
    }

    public static String findNameById(int id){
        switch(id){
            case 0:
                return "Chara";
            default:
                return "";
        }
    }
}

