package hashcode2017;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

public class Main
{
	/* Possible filenames are 'small', 'med1', 'med2', and 'big'. */
	private static final String	INPUT_FILENAME	= "input/hashcode2017/big.in";
	private static final String	OUTPUT_FILENAME	= "output/hashcode2017/big.out";

	static int		V, E, R, C, X;	/* Number of Videos, Endpoints, Requests, Cache Servers, and Capacity of Cache Servers. */
	static int[]	vidSizes;		/* Size of each video (in MB). */
	static int[][]	latencies;		/* Array of latencies between a cache server and the endpoint. */
	static int[][]	requests;		/* Array of requests of endpoint for each video. */

	public static void main ( String[] args )
	{
		getInput();
		ArrayList<Integer>[] videosToStoreInCache = assignVideosToCaches();
		output( videosToStoreInCache );
	}

	/** Get input from INPUT_FILENAME. */
	private static void getInput ()
	{
		int numConnections;
		int[] intArray; /* Used to get and assign input. */

		try ( BufferedReader br = new BufferedReader( new FileReader( new File( INPUT_FILENAME ) ) ) )
		{
			/* Create lambda function to get input line as array of ints. */
			Supplier<int[]> nextLineAsIntArray = () -> {
				try
				{
					return Arrays.asList( br.readLine().trim().split( "\\s+" ) ).stream().mapToInt( Integer::parseInt ).toArray();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
				return null;
			};

			intArray = nextLineAsIntArray.get();

			V = intArray[ 0 ]; /* Number of Videos. */
			E = intArray[ 1 ]; /* Number of Endpoints. */
			R = intArray[ 2 ]; /* Number of Request Descriptions. */
			C = intArray[ 3 ]; /* Number of Cache Servers. */
			X = intArray[ 4 ]; /* Capacity of the Cache Servers (in MB). */

			vidSizes = nextLineAsIntArray.get(); /* Get the size of each video (in MB). */

			latencies = new int[ C + 1 ][ E ]; /* Array of latencies between a cache server and the endpoint. */
			for ( int[] row : latencies )
			{
				Arrays.fill( row, Integer.MAX_VALUE ); /* Initially there is no connection. */
			}

			for ( int i = 0; i < E; ++i ) /* For each endpoint... */
			{
				intArray = nextLineAsIntArray
						.get(); /* Find out the latency to the MainFrame and how many connections to Cache servers there are. */

				latencies[ C ][ i ] = intArray[ 0 ]; /* Latency of endpoint to MainFrame. */
				numConnections = intArray[ 1 ]; /* How many Cache servers there are connected to the endpoint. */

				for ( int j = 0; j < numConnections; ++j ) /* For each connection to a Cache server... */
				{
					intArray = nextLineAsIntArray.get();

					latencies[ intArray[ 0 ] ][ i ] = intArray[ 1 ]; /* Latency from this Cache server to the endpoint is set. */
				}
			}

			requests = new int[ E ][ V ]; /* Each endpoint has requests for each video as zero initially. */

			for ( int i = 0; i < R; ++i ) /* For each request... */
			{
				intArray = nextLineAsIntArray.get();
				requests[ intArray[ 1 ] ][ intArray[ 0 ] ] += intArray[ 2 ]; /* The requests for vidId at endptId increased. */
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return;
		}
	}

	/** Assign the videos to the caches. */
	private static ArrayList<Integer>[] assignVideosToCaches ()
	{
		int[][] currentQuickestLatencies = new int[ E ][ V ]; /* The lowest latency with which an endpoint can get a video. This is dependent on the caches and therefore changes. */

		/* Initially the quickest way for an endpoint to get a video is from the MainFrame. */
		for ( int i = 0; i < E; i++ )
		{
			for ( int j = 0; j < V; j++ )
			{
				currentQuickestLatencies[ i ][ j ] = latencies[ C ][ i ];
			}
		}

		int[] knapsack = new int[ X + 1 ]; /* Knapsack array to place video in cache. */
		int[] kValues = new int[ V ]; /* The value of each video for the cache. */

		@SuppressWarnings ( "unchecked" )
		ArrayList<Integer>[] videosToStoreInCache = new ArrayList[ C ];

		System.out.println( "Completition:\n0.0%" );
		for ( int i = 0; i < C; i++ ) /* For each cache... */
		{
			Arrays.fill( knapsack, 0 );

			videosToStoreInCache[ i ] = new ArrayList<Integer>();

			calculateKnapsackValues( kValues, currentQuickestLatencies, i );
			solveKnapsack( knapsack, kValues );
			videosToStoreInCache[ i ] = recoverKnapsackSolution( knapsack, kValues );

			/* Update quickest way to access a video for an endpoint. */
			for ( int j = 0; j < videosToStoreInCache[ i ].size(); j++ )
			{
				for ( int k = 0; k < E; k++ )
				{
					currentQuickestLatencies[ k ][ videosToStoreInCache[ i ].get( j ) ] = Math
							.min( currentQuickestLatencies[ k ][ videosToStoreInCache[ i ].get( j ) ], latencies[ i ][ k ] );
				}
			}

			/* Print progress if a multiple of 0.1%. */
			if ( ( i + 1 ) * 1000 % C == 0 )
			{
				System.out.println( ( i + 1 ) * 100.0 / C + "%" );
			}
		}
		return videosToStoreInCache;
	}

	/** Work out the knapsack values for each video with respect to this cache. */
	private static void calculateKnapsackValues ( int[] kValues, int[][] quickLatencies, int cacheNumber )
	{
		for ( int j = 0; j < V; j++ ) /* For each video... */
		{
			kValues[ j ] = 0;
			for ( int k = 0; k < E; k++ ) /* For each endpoint... */
			{
				/* Add to the value of video j a score -- the difference between the fastest latency and the MainFrame, multipled by the number of requests. */
				kValues[ j ] += ( quickLatencies[ k ][ j ] - Math.min( latencies[ cacheNumber ][ k ], quickLatencies[ k ][ j ] ) )
						* requests[ k ][ j ];
			}
		}
	}

	/** Solve the knapsack problem. */
	private static void solveKnapsack ( int[] knapsack, int[] kValues )
	{
		if ( kValues[ 0 ] <= X )
		{
			knapsack[ vidSizes[ 0 ] ] = X;
		}

		for ( int a = 1; a < V; a++ )
		{
			for ( int b = X; b >= 0; b-- )
			{
				if ( b - vidSizes[ a ] >= 0 )
				{
					knapsack[ b ] = Math.max( knapsack[ b ], knapsack[ b - vidSizes[ a ] ] + kValues[ a ] );
				}
			}
		}
	}

	/** Create list of videos to add based on knapsack solution. */
	private static ArrayList<Integer> recoverKnapsackSolution ( int[] knapsack, int[] kValues )
	{
		@SuppressWarnings ( "unchecked" )
		ArrayList<Integer>[] knapsackSolution = new ArrayList[ X + 1 ]; /* First entry is always the weight. */

		for ( int y = X; y >= 0; y-- )
		{
			knapsackSolution[ y ] = new ArrayList<Integer>();
			knapsackSolution[ y ].add( Integer.MAX_VALUE );
		}

		knapsackSolution[ X ].set( 0, knapsack[ X ] );

		for ( int x = V - 1; x >= 0; x-- )
		{
			for ( int y = vidSizes[ x ]; y <= X; y++ )
			{
				if ( knapsackSolution[ y ].get( 0 ) - kValues[ x ] < knapsackSolution[ y - vidSizes[ x ] ].get( 0 ) )
				{
					knapsackSolution[ y - vidSizes[ x ] ].clear();

					knapsackSolution[ y - vidSizes[ x ] ].add( knapsackSolution[ y ].get( 0 ) - kValues[ x ] );
					for ( int z = 1; z < knapsackSolution[ y ].size(); z++ )
					{
						knapsackSolution[ y - vidSizes[ x ] ].add( knapsackSolution[ y ].get( z ) );
					}
					knapsackSolution[ y - vidSizes[ x ] ].add( x );
				}
			}
		}

		knapsackSolution[ 0 ].remove( 0 );

		return knapsackSolution[ 0 ];
	}

	/** Output assignment of videos to caches found. */
	private static void output ( ArrayList<Integer>[] videosStoredInCaches )
	{
		try ( BufferedWriter bw = new BufferedWriter( new FileWriter( OUTPUT_FILENAME ) ) )
		{
			bw.write( "" + videosStoredInCaches.length );
			for ( int i = 0; i < videosStoredInCaches.length; i++ )
			{
				bw.write( "\n" + i );
				for ( int j = videosStoredInCaches[ i ].size() - 1; j >= 0; j-- )
				{
					bw.write( " " + videosStoredInCaches[ i ].get( j ) );
				}
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
