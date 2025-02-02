package com.example.alonesns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.LauncherActivityInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alonesns.Model.MainModel;
import com.example.alonesns.Room.RoomDB;

import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    List<MainModel> items;
    Activity activityContext;
    RoomDB roomDB;
    Context context;

    String picturePath;

    int itemClickToggle = 0;

    public ItemAdapter(Activity activityContext, List<MainModel> items) {
        this.activityContext = activityContext;
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = (LayoutInflater.from(viewGroup.getContext())).inflate(R.layout.card_item, viewGroup, false);
        context = viewGroup.getContext();
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        MainModel item = items.get(position);
        roomDB = RoomDB.getInstance(activityContext);

        holder.dateTv.setText(item.getDate());

        picturePath = item.getPicture();
        holder.imageView.setImageURI(Uri.parse("file://" + picturePath));

        holder.contentTv.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        TextView dateTv, contentTv;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTv = itemView.findViewById(R.id.dateTv);
            imageView = itemView.findViewById(R.id.imageView);
            PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
            contentTv = itemView.findViewById(R.id.contentTv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickToggle == 0) {
                        contentTv.setMaxLines(Integer.MAX_VALUE);
                        contentTv.setEllipsize(null);
                        itemClickToggle++;
                    } else {
                        contentTv.setMaxLines(3);
                        contentTv.setEllipsize(TextUtils.TruncateAt.END);
                        itemClickToggle--;
                    }
                }
            });

            itemView.setOnCreateContextMenuListener(this); // 리스너를 현재 클래스에서 구현한다고 설정함
        }

        @Override
        public void onClick(View v) {}

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // Context 메뉴를 생성하고 메뉴 항목 선택 시 호출되는 리스너를 등록함
            MenuItem edit = menu.add(Menu.NONE, 101, 1, "수정");
            MenuItem delete = menu.add(Menu.NONE, 102, 2, "삭제");
            edit.setOnMenuItemClickListener(menuItemClickListener);
            delete.setOnMenuItemClickListener(menuItemClickListener);
        }

        // Context 메뉴에서 항목 클릭 시 동작을 설정함
        MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 101: // 수정 항목 클릭 시
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View view = LayoutInflater.from(context).inflate(R.layout.edt_dialog, null, false);
                        builder.setView(view);
                        AlertDialog dialog = builder.create();

                        EditText edt = (EditText) view.findViewById(R.id.edt);
                        // 해당 아이템에 입력되어 있던 데이터를 불러와서 대화상자에 표시함
                        edt.setText(items.get(getAdapterPosition()).getContent());

                        Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               dialog.dismiss();
                            }
                        });

                        Button editBtn = (Button) view.findViewById(R.id.editBtn);
                        editBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainModel item = items.get(getAdapterPosition());
                                int id = item.get_id();
                                String strContent = edt.getText().toString();
                                roomDB.mainDao().update(id, strContent);

                                items.clear();
                                items.addAll(roomDB.mainDao().getAll());
                                notifyDataSetChanged();
                                Toast.makeText(context, "수정되었습니다. ", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;

                    case 102: // 삭제 항목 클릭 시
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setTitle("삭제");
                        builder1.setMessage("선택한 게시물을 정말 삭제하시겠습니까 ?");
                        builder1.setIcon(R.drawable.delete);

                        builder1.setPositiveButton("삭제하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem(getAdapterPosition());
                                Toast.makeText(context, "삭제되었습니다. ", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder1.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder1.show();
                        return true;
                }
                return true;
            }
        };
    }

    public void deleteItem(int position) {
        MainModel item = items.get(position);
        roomDB.mainDao().delete(item);

        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }
}
