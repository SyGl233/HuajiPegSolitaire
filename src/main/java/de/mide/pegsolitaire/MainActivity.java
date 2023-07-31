package de.mide.pegsolitaire;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static de.mide.pegsolitaire.model.PlaceStatusEnum.SPACE;
import static de.mide.pegsolitaire.model.PlaceStatusEnum.PEG;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Space;
import android.widget.Toast;

import java.util.Arrays;

import de.mide.pegsolitaire.model.Boards;
import de.mide.pegsolitaire.model.PlaceStatusEnum;
import de.mide.pegsolitaire.model.SpacePosition;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    public static final String TAG4LOGGING = "PegSolitaire";

    private static final int TEXT_COLOR_BROWN = 0xffa52a2a;
    private static final int TEXT_COLOR_RED = 0xffff0000;

    /**
     * Unicode字符：实心方块
     */
    private static final String TOKEN_MARK = "■";



    /**
     * 用于存储棋盘初始化的数组。
     */
    private static PlaceStatusEnum[][] PLACE_INIT_ARRAY;

    private int _sizeColumn;

    private int _sizeRow;

    /**
     * 用于存储棋盘上的棋子和空位置的数组。
     */
    private PlaceStatusEnum[][] _placeArray = null;

    /**
     * 当前棋盘上的棋子数量。
     */
    private int _numberOfPegs = -1;
    /**
     * 当前执行的步数。
     */
    private int _numberOfSteps = -1;
    /**
     * 选中的棋子是否已经被移动了。
     */
    private boolean _selectedPegMoved = false;

    /**
     * 用于存储棋盘上的棋子的按钮。
     */
    private ViewGroup.LayoutParams _buttonLayoutParams = null;

    /**
     * 用于开始新游戏的按钮。
     */
    private Button _startButton = null;

    /**
     * 棋盘上的棋子和空位置的布局。
     */
    private GridLayout _gridLayout = null;

    /**
     * 被选中的棋子横纵坐标，无被选中时设为(-1,-1)
     */
    private SpacePosition _selectedP = new SpacePosition(-1,-1);

    /**
     * 记录是否有选中的棋子
     */
    private boolean _selected = false;

    /**
     * 资源相关
     */
    private Resources _resources = null;
    private Drawable _drawable_peg = null;
    private Drawable _drawable_peg_chosen = null;
    private Drawable _drawable_space = null;
    private Drawable _drawable_icon = null;

    /**
     * 名人堂相关
     */

    private int _current_board = 3;
    private SharedPreferences _preferences = null;
    private SharedPreferences.Editor _editor = null;

    /**
     * 用于处理点击棋盘上的棋子的事件。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        PLACE_INIT_ARRAY = Boards.BOARD_CROSS;
        _sizeRow = PLACE_INIT_ARRAY[0].length;
        _sizeColumn = PLACE_INIT_ARRAY.length;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        _resources = getResources();
        _drawable_peg = _resources.getDrawable(R.drawable.peg);
        _drawable_peg_chosen = _resources.getDrawable(R.drawable.peg_chosen);
        _drawable_space = _resources.getDrawable(R.drawable.space);
        _drawable_icon = _resources.getDrawable(R.drawable.icon);

        _preferences = getPreferences(MODE_PRIVATE);
        _editor = _preferences.edit();

        Log.i(TAG4LOGGING, "column=" + _sizeColumn + ", row=" + _sizeRow + "px:");

        _gridLayout = findViewById(R.id.boardGridLayout);

        actionBarConfiguration();
        choose_board();
    }

    /**
     * 从显示器读取分辨率并将值写入适当的成员变量。
     */
    private void displayResolutionEvaluate() {

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        Log.i(TAG4LOGGING, "Display-Resolution: " + displayWidth + "x" + displayHeight);

        int _sideLengthPlace = displayWidth / _sizeColumn;

        _buttonLayoutParams = new ViewGroup.LayoutParams(_sideLengthPlace,
                _sideLengthPlace);
    }

    /**
     * 初始化操作栏。
     */
    private void actionBarConfiguration() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {

            Toast.makeText(this, "没有操作栏", Toast.LENGTH_LONG).show();
            return;
        }

        actionBar.setTitle("单人跳棋");
    }

    /**
     * 从资源文件加载操作栏菜单项。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu_items, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 处理操作栏菜单项的选择。
     * 在扩展的版本中，你需要加入更多的菜单项。
     *
     * @param item 选择的菜单项
     * @return true: 选择的菜单项被处理了
     * false: 选择的菜单项没有被处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_new_game) {

            selectedNewGame();
            return true;

        } else if(item.getItemId() == R.id.show_records) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("名人堂");
            builder.setIcon(_drawable_icon);
            String str = "";
            for(int i=0;i<Boards.BOARD_NAMES.length;i++){
                str+=Boards.BOARD_NAMES[i];
                str+=":   ";
                int res = _preferences.getInt(Boards.BOARD_NAMES[i], -1);
                str+=((res==-1)?"无记录":String.valueOf(res));
                str+="\n";
            }
            builder.setMessage(str);
            builder.setPositiveButton("确定",(DialogInterface.OnClickListener) (dialog, which)->{
                dialog.cancel();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    /**
     * 处理点击"新游戏"按钮的事件。
     * 弹出对话框，询问用户是否要开始新游戏。
     * 如果用户选择"是"，则初始化棋盘，否则不做任何事情。
     */
    public void selectedNewGame() {
        // TODO
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("开始新游戏");
        builder.setTitle("单人跳棋");
        builder.setIcon(_drawable_icon);
        builder.setPositiveButton("是", (DialogInterface.OnClickListener) (dialog, which) -> {
            //当按下是，初始化棋盘
            dialog.cancel();

            //初始化棋盘，见下函数
            choose_board();

        });
        builder.setNegativeButton("否",(DialogInterface.OnClickListener) (dialog,which) ->{
            //当按下否，什么都不做
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * 初始化棋盘上的棋子和空位置。
     */
    private void initializeBoard(int board_id) {
        _current_board = board_id;
        PLACE_INIT_ARRAY = Boards.BOARDS[board_id];
        _sizeColumn = PLACE_INIT_ARRAY.length;
        _sizeRow = PLACE_INIT_ARRAY[0].length;
        Log.i(TAG4LOGGING,"Columns: "+_sizeColumn);

        displayResolutionEvaluate();

        if (_gridLayout.getRowCount() == 0) {

            _gridLayout.setColumnCount(_sizeColumn);
            _gridLayout.setRowCount(_sizeRow);

        } else { // 清除旧的棋盘

            _gridLayout.removeAllViews();
            _gridLayout.setColumnCount(_sizeColumn);
            _gridLayout.setRowCount(_sizeRow);
        }


        _selected = false;
        _numberOfSteps = 0;
        _numberOfPegs = 0;
        _selectedPegMoved = false;
        _placeArray = new PlaceStatusEnum[_sizeColumn][_sizeRow];

        for (int i = 0; i < _sizeColumn; i++) {

            for (int j = 0; j < _sizeRow; j++) {

                PlaceStatusEnum placeStatus = PLACE_INIT_ARRAY[i][j];

                _placeArray[i][j] = placeStatus;

                switch (placeStatus) {

                    case PEG:
                        generateButton(i, j, true);
                        break;

                    case SPACE:
                        generateButton(i, j, false);
                        break;

                    case BLOCKED:
                        Space space = new Space(this); // Dummy-Element
                        _gridLayout.addView(space);
                        break;

                    default:
                        Log.e(TAG4LOGGING, "错误的棋盘状态");

                }
            }
        }

        _selectedP = new SpacePosition(-1,-1);

        Log.i(TAG4LOGGING, "棋盘初始化完成");
        updateDisplayStepsNumber();
    }

    /**
     * 生成棋盘上的一个位置。
     * 在基础任务中，棋盘上的棋子直接用字符 TOKEN_MARK 表示。
     * 在扩展任务中，棋盘上的棋子用图片表示。
     */
    private void generateButton(int indexColumn, int indexRow, boolean isPeg) {

        ImageButton button = new ImageButton(this);

        button.setLayoutParams(_buttonLayoutParams);
        button.setOnClickListener(this);

        SpacePosition pos = new SpacePosition(indexColumn, indexRow);
        button.setTag(pos);

        // TODO
        if(isPeg){
            button.setForeground(_drawable_peg);
            _numberOfPegs++;
        }else{
            button.setForeground(_drawable_space);
        }
        _gridLayout.addView(button);
    }


    /**
     * 更新操作栏中的步数显示。
     */
    private void updateDisplayStepsNumber() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("执行步数：" + _numberOfSteps);
        }
    }

    /**
     * 处理棋盘上的点击事件。
     * 如果被点击的按钮是一个棋子，那么它将被改变选中状态。
     * 也就是说，如果它之前没有被选中，这个棋子会变为红色，
     * 同时，此前被选中的棋子（如果有）将变为棕色。
     * 或者如果它已经被选中，那么它自己将变为棕色。
     * 如果被点击的按钮是一个空位置，那么试图将被选中的棋子移动到该位置。
     * 如果移动成功，你需要更新棋盘上的棋子和空位置。
     * 如果移动失败，你需要显示一个错误信息。
     *
     * @param view 被点击的按钮
     *

     */
    @Override
    public void onClick(View view) {

        ImageButton clickedButton = (ImageButton) view;

        SpacePosition targetPosition = (SpacePosition) clickedButton.getTag();

        // 获取被点击的按钮的位置
        int indexColumn = targetPosition.getIndexColumn();
        int indexRow = targetPosition.getIndexRow();
        PlaceStatusEnum placeStatus = _placeArray[indexColumn][indexRow];

        switch (placeStatus) {

            case PEG:
                // TODO
                if(_selected){
                    int _selectedR = _selectedP.getIndexRow();
                    int _selectedC = _selectedP.getIndexColumn();
                    if(_selectedR == -1){
                        Log.e(TAG4LOGGING,"选中了吗？如选");
                    }else if(_selectedP.equals(targetPosition)){
                        _selected = false;
                        _selectedPegMoved = false;
                        clickedButton.setForeground(_drawable_peg);
                    }else{
                        _selectedPegMoved = false;
                        getButtonFromPosition(_selectedP).setForeground(_drawable_peg);
                        //Log.i(TAG4LOGGING,"理论坐标："+_selectedR+" "+_selectedC+"\n真实坐标："+((SpacePosition)selected.getTag()).getIndexRow()+" "+((SpacePosition)selected.getTag()).getIndexColumn());
                        _selectedP = targetPosition;
                        clickedButton.setForeground(_drawable_peg_chosen);
                    }
                }else{
                    _selected = true;
                    _selectedPegMoved = false;
                    _selectedP = targetPosition;
                    clickedButton.setForeground(_drawable_peg_chosen);
                }
                break;

            case SPACE:
                // TODO
                if(_selected){
                    SpacePosition skipped = getSkippedPosition(_selectedP,targetPosition);
                    if(skipped!=null){
                        Log.i(TAG4LOGGING,"可以跳捏！！");
                        jumpToPosition(getButtonFromPosition(_selectedP),clickedButton,getButtonFromPosition(skipped));
                    }else{
                        Log.i(TAG4LOGGING,"返回NULL！！！");
                    }
                }else{
                    Log.i(TAG4LOGGING,"Not Selected!!!");
                }
                break;

            default:
                Log.e(TAG4LOGGING, "错误的棋盘状态" + placeStatus);
        }
    }

    /**
     * 执行跳跃。仅当确定移动合法时才可以调用该方法。
     * 数组中三个位置的状态，和总棋子数发生变化。
     * 同时，在移动后，你需要检查是否已经结束游戏。
     *
     * @param startButton 被选中的棋子
     * @param targetButton 被选中的空位置
     * @param skippedButton 被跳过的棋子
     *
     */
    private void jumpToPosition(ImageButton startButton, ImageButton targetButton, ImageButton skippedButton) {

        // TODO

        //改变显示
        startButton.setForeground(_drawable_space);
        targetButton.setForeground(_drawable_peg_chosen);
        skippedButton.setForeground(_drawable_space);

        int r1 = ((SpacePosition)startButton.getTag()).getIndexRow();
        int r2 = ((SpacePosition)targetButton.getTag()).getIndexRow();
        int r3 = ((SpacePosition)skippedButton.getTag()).getIndexRow();
        int c1 = ((SpacePosition)startButton.getTag()).getIndexColumn();
        int c2 = ((SpacePosition)targetButton.getTag()).getIndexColumn();
        int c3 = ((SpacePosition)skippedButton.getTag()).getIndexColumn();

        _placeArray[c1][r1] = SPACE;
        _placeArray[c2][r2] = PEG;
        _placeArray[c3][r3] = SPACE;

        Log.i(TAG4LOGGING,"(3,3):"+_placeArray[3][3]);

        //改变数据
        _numberOfPegs--;
        if(!_selectedPegMoved) {
            _numberOfSteps++;
        }
        _selected = true;
        _selectedP = (SpacePosition) targetButton.getTag();
        _selectedPegMoved = true;
        updateDisplayStepsNumber();
        if (_numberOfPegs == 1) {
            showVictoryDialog();
        } else if (!has_movable_places()) {
            showFailureDialog();
        }
    }

    /**
     * 返回位置对应的按钮。
     *
     * @param position 位置
     * @return 按钮
     */
    private ImageButton getButtonFromPosition(SpacePosition position) {

        int index = position.getPlaceIndex(_sizeRow);

        return (ImageButton) _gridLayout.getChildAt(index);
    }

    /**
     * 显示一个对话框，表明游戏已经胜利（只剩下一个棋子）。
     * 点击对话框上的按钮，可以重新开始游戏。
     * 在扩展版本中，你需要在这里添加一个输入框，让用户输入他的名字。
     */
    private void showVictoryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("胜利");
        dialogBuilder.setMessage("你赢了！");
        dialogBuilder.setPositiveButton("再来一局", (dialogInterface, i) -> {
            choose_board();  // 重新开始游戏
        });
        dialogBuilder.setIcon(_drawable_icon);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        int minSteps = _preferences.getInt(Boards.BOARD_NAMES[_current_board], -1);
        if(minSteps == -1){
            _editor.putInt(Boards.BOARD_NAMES[_current_board], _numberOfSteps);
        }else{
            if(minSteps>_numberOfSteps){
                _editor.putInt(Boards.BOARD_NAMES[_current_board], _numberOfSteps);
            }
        }
        _editor.commit();

    }

    /**
     * 显示一个对话框，表明游戏已经失败（没有可移动的棋子）。
     * 点击对话框上的按钮，可以重新开始游戏。
     */
    private void showFailureDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("失败");
        dialogBuilder.setMessage("你输了！");
        dialogBuilder.setPositiveButton("再来一局", (dialogInterface, i) -> {
            choose_board();  // 重新开始游戏
        });
        dialogBuilder.setIcon(_drawable_icon);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * 给定一个起始位置和目标位置。
     * 如果移动合法，返回被跳过的位置。
     * 如果移动不合法，返回 {@code null}。
     * 移动合法的定义，参见作业文档。
     *
     * @param startPos  起始位置
     * @param targetPos 目标位置
     * @return 移动合法时，返回一个新{@code SpacePosition}
     * 表示被跳过的位置；否则返回 {@code null}
     */
    private SpacePosition getSkippedPosition(SpacePosition startPos, SpacePosition targetPos) {
        // TODO
        int r1 = startPos.getIndexRow();
        int c1 = startPos.getIndexColumn();
        int r2 = targetPos.getIndexRow();
        int c2 = targetPos.getIndexColumn();
        if(r1==r2){
            if(c1-c2==2 || c2-c1==2){
                if(_placeArray[(c1+c2)/2][r1]==PEG){
                    return new SpacePosition((c1+c2)/2,r1);
                }
            }
        }else if(c1==c2){
            if(r1-r2==2 || r2-r1==2){
                if(_placeArray[c1][(r1+r2)/2]==PEG){
                    return new SpacePosition(c1,(r1+r2)/2);
                }
            }
        }
        return null;
    }

    /**
     * 返回是否还有可移动的位置。
     *
     * @return 如果还有可移动的位置，返回 {@code true}
     * 否则返回 {@code false}
     */
    private Boolean has_movable_places(){
        for(int i = 0; i < _sizeColumn; i++){
            for(int j = 0; j < _sizeRow; j++){
                if(_placeArray[i][j] == PEG){
                    // TODO
                    int dx[] = {0,0,2,-2};
                    int dy[] = {2,-2,0,0};
                    for(int k=0;k<4;k++){
                        int x = i+dx[k];
                        int y = j+dy[k];
                        if(x<0 || x>=_sizeColumn)continue;
                        if(y<0 || y>=_sizeRow)continue;
                        if(_placeArray[x][y]!=SPACE)continue;
                        if(getSkippedPosition(new SpacePosition(i,j),new SpacePosition(x,y))!=null){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    private void choose_board(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("选择棋盘");
        builder.setSingleChoiceItems(Boards.BOARD_NAMES, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                initializeBoard(i);
            }
        });
        builder.setIcon(_drawable_icon);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
