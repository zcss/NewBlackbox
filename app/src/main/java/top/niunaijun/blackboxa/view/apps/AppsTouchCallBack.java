package top.niunaijun.blackboxa.view.apps;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AppsTouchCallBack extends ItemTouchHelper.Callback {
    private static final String TAG = "AppsTouchCallBack";
    public interface MoveListener { void onMove(int from, int to); }
    private final MoveListener onMoveBlock;

    public AppsTouchCallBack(MoveListener onMoveBlock) { this.onMoveBlock = onMoveBlock; }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        try {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting movement flags: " + e.getMessage());
            return makeMovementFlags(0, 0);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        try {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();
            if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                Log.w(TAG, "Invalid positions: from=" + fromPosition + ", to=" + toPosition);
                return false;
            } else if (fromPosition == toPosition) {
                return false;
            } else {
                onMoveBlock.onMove(fromPosition, toPosition);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onMove: " + e.getMessage());
            return false;
        }
    }

    @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

    @Override
    public void onSelectedChanged(@androidx.annotation.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        try {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                if (viewHolder != null) viewHolder.itemView.setAlpha(0.8f);
            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder != null) viewHolder.itemView.setAlpha(1.0f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onSelectedChanged: " + e.getMessage());
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        try {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
        } catch (Exception e) {
            Log.e(TAG, "Error in clearView: " + e.getMessage());
        }
    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
        try {
            int targetPosition = target.getBindingAdapterPosition();
            return targetPosition != RecyclerView.NO_POSITION;
        } catch (Exception e) {
            Log.e(TAG, "Error in canDropOver: " + e.getMessage());
            return false;
        }
    }
}
