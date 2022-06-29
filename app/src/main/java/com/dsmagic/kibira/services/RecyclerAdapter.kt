package com.dsmagic.kibira.services

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.dsmagic.kibira.R

class RecyclerAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        // create new views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // inflates the card_view_design view
            // that is used to hold list item
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_view_design, parent, false)

            return ViewHolder(view)
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val ItemsViewModel = mList[position]

            // sets the image to the imageview from our itemHolder class
            holder.imageView.setImageResource(ItemsViewModel.image)

            // sets the text to the textview from our itemHolder class
            holder.textView.text = ItemsViewModel.text

        }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return mList.size
        }

        // Holds the views for adding it to image and text
        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageview)
            val textView: TextView = itemView.findViewById(R.id.textView)
        }


}
//
//
//class RecyclerAdapter(private val dataSet: Array<String>) :
//    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
//
//    /**
//     * Provide a reference to the type of views that you are using
//     * (custom ViewHolder).
//     */
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val textView: TextView
//
//        init {
//            // Define click listener for the ViewHolder's View.
//            textView = view.findViewById(R.id.textView)
//        }
//    }
//
//    // Create new views (invoked by the layout manager)
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        // Create a new view, which defines the UI of the list item
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.list_projects, viewGroup, false)
//
//        return ViewHolder(view)
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        // Get element from your dataset at this position and replace the
//        // contents of the view with that element
//        viewHolder.textView.text = dataSet[position]
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    override fun getItemCount() = dataSet.size
//

