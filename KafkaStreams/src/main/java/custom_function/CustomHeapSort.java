package custom_function;

import lombok.Data;
import model.ResultPojo;
import model.SnappyTuple3;

import java.io.Serializable;
import java.util.ArrayList;


@Data
public class CustomHeapSort implements Serializable {

    private ArrayList<ResultPojo> list;

    public CustomHeapSort(){ }

    public CustomHeapSort(ArrayList<ResultPojo> list){
        this.list = list;
    }


    public void sort() {
        int n = list.size();

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(list, n, i);

        // One by one extract an element from heap
        for (int i=n-1; i>0; i--){
            // Move current root to end
            ResultPojo temp = list.get(0);
            list.set(0, list.get(i));
            list.set(i, temp);

            // call max heapify on the reduced heap
            heapify(list, i, 0);
        }
    }

    // To heapify a subtree rooted with node i which is
    // an index in arr[]. n is size of heap
    private void heapify(ArrayList<ResultPojo> list, int n, int i){
        int largest = i; // Initialize largest as root
        int l = 2*i + 1; // left = 2*i + 1
        int r = 2*i + 2; // right = 2*i + 2

        // If left child is larger than root
        if (l < n && list.get(l).getCount() > list.get(largest).getCount())
            largest = l;

        // If right child is larger than largest so far
        if (r < n && list.get(r).getCount() > list.get(largest).getCount())
            largest = r;

        // If largest is not root
        if (largest != i){
            ResultPojo swap = list.get(i);
            list.set(i,list.get(largest));
            list.set(largest, swap);

            // Recursively heapify the affected sub-tree
            heapify(list, n, largest);
        }
    }


    public void printArray()
    {
        int n = 3;
        for (int i=0; i<n; ++i)
            System.out.print(this.list.get(i).toString()+" ");
        System.out.println();
    }


}



