import java.io.*;
import java.util.*;

public class SortingTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			boolean isRandom = false;	// 입력받은 배열이 난수인가 아닌가?
			int[] value;	// 입력 받을 숫자들의 배열
			String nums = br.readLine();	// 첫 줄을 입력 받음
			if (nums.charAt(0) == 'r')
			{
				// 난수일 경우
				isRandom = true;	// 난수임을 표시

				String[] nums_arg = nums.split(" ");

				int numsize = Integer.parseInt(nums_arg[1]);	// 총 갯수
				int rminimum = Integer.parseInt(nums_arg[2]);	// 최소값
				int rmaximum = Integer.parseInt(nums_arg[3]);	// 최대값

				Random rand = new Random();	// 난수 인스턴스를 생성한다.

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 각각의 배열에 난수를 생성하여 대입
					value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
			}
			else
			{
				// 난수가 아닐 경우
				int numsize = Integer.parseInt(nums);

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 한줄씩 입력받아 배열원소로 대입
					value[i] = Integer.parseInt(br.readLine());
			}

			// 숫자 입력을 다 받았으므로 정렬 방법을 받아 그에 맞는 정렬을 수행한다.
			while (true)
			{
				int[] newvalue = (int[])value.clone();	// 원래 값의 보호를 위해 복사본을 생성한다.
                char algo = ' ';

				if (args.length == 4) {
                    return;
                }

				String command = args.length > 0 ? args[0] : br.readLine();

				if (args.length > 0) {
                    args = new String[4];
                }
				
				long t = System.currentTimeMillis();
				switch (command.charAt(0))
				{
					case 'B':	// Bubble Sort
						newvalue = DoBubbleSort(newvalue);
						break;
					case 'I':	// Insertion Sort
						newvalue = DoInsertionSort(newvalue);
						break;
					case 'H':	// Heap Sort
						newvalue = DoHeapSort(newvalue);
						break;
					case 'M':	// Merge Sort
						newvalue = DoMergeSort(newvalue);
						break;
					case 'Q':	// Quick Sort
						newvalue = DoQuickSort(newvalue);
						break;
					case 'R':	// Radix Sort
						newvalue = DoRadixSort(newvalue);
						break;
					case 'S':	// Search
						algo = DoSearch(newvalue);
						break;
					case 'X':
						return;	// 프로그램을 종료한다.
					default:
						throw new IOException("잘못된 정렬 방법을 입력했습니다.");
				}
				if (isRandom)
				{
					// 난수일 경우 수행시간을 출력한다.
					System.out.println((System.currentTimeMillis() - t) + " ms");
				}
				else
				{
					// 난수가 아닐 경우 정렬된 결과값을 출력한다.
                    if (command.charAt(0) != 'S') {
                        for (int i = 0; i < newvalue.length; i++) {
                            System.out.println(newvalue[i]);
                        }
                    } else {
                        System.out.println(algo);
                    }
				}

			}
		}
		catch (IOException e)
		{
			System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoBubbleSort(int[] value)
	{
		// TODO : Bubble Sort 를 구현하라.
		int tmp=0;
		for (int i= value.length-1; i>0 ;i--){
			for (int j=0;j<i;j++){
				if(value[j]>value[j+1]){
					tmp=value[j];
					value[j]=value[j+1];
					value[j+1]=tmp;
				}
			}
		}
		// value는 정렬안된 숫자들의 배열이며 value.length 는 배열의 크기가 된다.
		// 결과로 정렬된 배열은 리턴해 주어야 하며, 두가지 방법이 있으므로 잘 생각해서 사용할것.
		// 주어진 value 배열에서 안의 값만을 바꾸고 value를 다시 리턴하거나
		// 같은 크기의 새로운 배열을 만들어 그 배열을 리턴할 수도 있다.
		
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoInsertionSort(int[] value)
	{
		// TODO : Insertion Sort 를 구현하라.
		int tmp=0;
		int j;
		for (int i = 1; i<value.length ;i++){
			tmp=value[i];
			for (j=i-1;j>=0;j--){
				if(tmp>=value[j]){
					break;
				}
				value[j+1]=value[j];
			}
			value[j+1]=tmp;
		}
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoHeapSort(int[] value)
	{
		// TODO : Heap Sort 를 구현하라.
		for(int i=(value.length-1)/2;i>=0;i--){
			PerculateDown(value,i,value.length-1);
		}
		int tmp;
		for(int i=value.length-1;i>0;i--){
			tmp=value[i];
			value[i]=value[0];
			value[0]=tmp;
			PerculateDown(value,0,i-1);
		}
		return (value);
	}

	private static void PerculateDown(int[] value,int idx,int end)
	{
		if (idx*2>end) return;
		int tmp=value[idx];
		int biggeridx=idx;
		if(value[idx]<value[idx*2]){
			tmp=value[idx*2];
			biggeridx=idx*2;
		}
		if (idx*2+1<=end){
			if(value[idx]<value[idx*2+1]){
				if(tmp<value[idx*2+1]){
					tmp=value[idx*2+1];
					biggeridx=idx*2+1;
				}
			}
		}

		if(biggeridx==idx) return;
		value[biggeridx]=value[idx];
		value[idx]=tmp;

		PerculateDown(value,biggeridx,end);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoMergeSort(int[] value)
	{
		// TODO : Merge Sort 를 구현하라.
		int[] tmp=new int[value.length];
		MergeSort(value,tmp,0,value.length-1);

		return (value);
	}
	private static void MergeSort(int[]value, int[] tmp, int start, int end){
		if(start<end){
			int mid=(start+end)/2;
			MergeSort(value,tmp,start,mid);
			MergeSort(value,tmp,mid+1,end);
			Merge(value,tmp,start,mid,end);
		}
		
	}
	private static void Merge(int[] value, int[] tmp, int start, int mid, int end){

		int i=start; int j=mid+1; int t=0;
		while(i<=mid&&j<=end){
			if(value[i]<value[j]) tmp[t++]=value[i++];
			else tmp[t++]=value[j++];
		}
		while(i<=mid){
			tmp[t++]=value[i++];
		}
		while(j<=end){
			tmp[t++]=value[j++];
		}
		i=start; t=0;
		while (i<=end){
			value[i++]=tmp[t++];
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoQuickSort(int[] value)
	{
		// TODO : Quick Sort 를 구현하라.
		QuickSort(value,0,value.length-1);
		return (value);
	}

	private static void QuickSort(int[] value, int start, int end){
		if(start<end){
			int p = partition(value,start,end);
			QuickSort(value,start,p-1);
			QuickSort(value,p+1,end);
		}
	}

	private static int partition(int[] value, int start, int end){
		int pivot=end;
		int pivotValue=value[pivot];
		int i=start-1;
		int tmp;
		for(int j=start;j<end;j++){
			if(value[j]<=pivotValue){
				tmp=value[j];
				value[j]=value[i+1];
				value[i+1]=tmp;
				i++;
			}
		}
		for(int j=end;j>i+1;j--){
			value[j]=value[j-1];
		}
		value[i+1]=pivotValue;
		pivot=i+1;
		return pivot;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoRadixSort(int[] value)
	{
		// TODO : Radix Sort 를 구현하라.

		int min=value[0];
		for(int i=1;i<value.length;i++){
			if(value[i]<min) min=value[i];
		}
		//0이상의 정수로 바꿔줌
		for(int i=0;i<value.length;i++){
			value[i]-=min;
		}

		int max=-1;
		for(int i=1;i<value.length;i++){
			if(value[i]>max) max=value[i];
		}

		int[] cnt = new int[10];
		int [] start = new int[10];
		int[] newvalue = new int[value.length];


		int maxDigits=1;
		while(max>=10){
			max=max/10;
			maxDigits*=10;
		}

		for(int digit = 1; digit <=maxDigits;digit*=10){
			for(int d=0;d<=9;d++){
				cnt[d]=0;
			}
			for(int i=0;i<value.length;i++){
				cnt[value[i]/digit%10]++;
			}
			start[0]=0;
			for(int d=1;d<=9;d++){
				start[d]=start[d-1]+cnt[d-1];
			}
			for(int i=0;i<value.length;i++){
				newvalue[start[value[i]/digit%10]++]=value[i];
			}
			for(int i=0;i<value.length;i++){
				value[i]=newvalue[i];
			}
		}

		for(int i=0;i<value.length;i++){
			value[i]+=min;
		}
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
    private static char DoSearch(int[] value)
	{
		int[] newvalue=value;
		Hashtable<Integer,Integer> duplicatedCntHashtable=new Hashtable<>();
		int maxDigits=0, duplicatedCnt=1 ,sortedCnt=1;
		float sortedRatio=0,duplicatedRatio=0;

		int min=newvalue[0];
		int max=newvalue[0];
		duplicatedCntHashtable.put(value[0],0);
		for(int i=1;i<newvalue.length;i++){
			if(newvalue[i]<min) min=newvalue[i];
			if(newvalue[i]>max) max=newvalue[i];
			if(newvalue[i-1]<=newvalue[i]) sortedCnt++;
			duplicatedCntHashtable.put(value[i],i);

		}
		duplicatedCnt=value.length-duplicatedCntHashtable.size();

		//0이상의 정수로 바꿔줌

		max-=min;

		maxDigits = (int)Math.log10(max)+1;
		if(max==0) maxDigits=1;
		duplicatedRatio=(float)duplicatedCnt/value.length;
		sortedRatio=(float)sortedCnt/value.length;

		//System.out.println("length= "+value.length+", maxDigits= "+maxDigits);
		//System.out.println("duplicatedRatio= "+duplicatedRatio+", sortedRatio= "+sortedRatio);

		if (sortedRatio>=1-Math.sqrt(maxDigits/value.length)){
			//System.out.println('I');
			return ('I');
		}

		if(duplicatedRatio>=0.5){
			//System.out.println('H');
			return ('H');
		}
		if(maxDigits<Math.log10(value.length)-2) {
			//System.out.println('R');
			return ('R');
		}

		//System.out.println('Q');
		return ('Q');




		// TODO : Search 를 구현하라.
	}

}
