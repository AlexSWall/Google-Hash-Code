package hashcode2018;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Alex
 *
 * To change the location of the input and output files, change the Files enum near the bottom of the file.
 */
public class HashCode2018
{
	// === Hyperparameters ===

	/* File to deal with —— { A = a_example, B = b_should_be_easy, C = c_no_hurry, D = d_metropolis, E = e_high_bonus }. */
	private static final Files file = Files.A;

	/* The power of the numerator in the score function. */
	private static final int POW = 1;

	/* The score function that calculates the score of a ride for a car — used when greedily trying to assign rides to cars. */
	private static final BiFunction<CarInfo, RideInfo, Double> scoreFunc = ( car, ride ) -> {
		return ( Math.pow( ride.dist + getBonusModifier( car, ride ), POW ) ) / ( getTotalCarJourneyTime( car, ride ) );
	};

	// === The problem instance variables ===

	static int	R;	/* Number of rows of the grid. */
	static int	C;	/* Number of columns of the grid. */
	static int	F;	/* Number of vehicles in the fleet. */
	static int	N;	/* Number of rides. */
	static int	B;	/* Per-ride bonus for starting the ride on time. */
	static int	T;	/* Number of steps in the simulation. */

	static List<RideInfo> rides;

	static CarInfo[] cars;

	public static void main ( String[] args )
	{
		getInput( file );
		List<List<Integer>> solution = getAssignment( scoreFunc );
		output( file, solution );
	}

	/**
	 * Gets the assignment of cars, indexed by ints, to the list of rides it will give, also index by ints.
	 *
	 * @param scoreFunc The function that calculates the score of a ride for a car — used when greedily trying to assign rides to cars.
	 * @return A list which has, indexed by the integer value of the corresponding car, the rides ids the car has been allocated.
	 */
	private static List<List<Integer>> getAssignment ( BiFunction<CarInfo, RideInfo, Double> scoreFunc )
	{
		List<List<Integer>> solution = new ArrayList<List<Integer>>();

		for ( int i = 0; i < F; i++ )
		{
			solution.add( new ArrayList<Integer>() );
		}

		CarInfo car;
		RideInfo ride;
		double maxScore, score;
		int maxRide;

		outerloop: for ( int t = 0; t < T; t++ )
		{
			for ( int r = 0; r < rides.size(); r++ )
			{
				if ( rides.get( r ).f <= t )
				{
					rides.remove( rides.get( r ) );
				}
			}

			for ( int c = 0; c < cars.length; c++ )
			{
				if ( rides.size() == 0 )
				{
					break outerloop;
				}
				car = cars[ c ];

				/* Ensure car is available for this timestep. */
				if ( car.t > t )
				{
					continue;
				}
				else if ( car.t < t )
				{
					car.t = t;
				}

				maxScore = -2;
				maxRide = -2;

				/* For available car, decide on a ride. */
				for ( int r = 0; r < rides.size(); r++ )
				{
					ride = rides.get( r );
					/* The ride may not be possible. */
					if ( isJourneyInfeasible( car, ride ) )
					{
						continue;
					}

					score = scoreFunc.apply( car, ride );

					if ( score > maxScore )
					{
						maxScore = score;
						maxRide = r;
					}
				}

				if ( maxRide == -2 )
				{
					continue;
				}

				ride = rides.get( maxRide );

				solution.get( c ).add( ride.rideID );

				car.t += getTotalCarJourneyTime( car, ride );
				car.x = ride.x;
				car.y = ride.y;

				rides.remove( rides.get( maxRide ) );
			}
		}

		return solution;
	}

	/** Get the potential bonus modifier if the car gives such a ride. */
	private static double getBonusModifier ( CarInfo car, RideInfo ride )
	{
		return distToStart( car, ride ) <= ( car.t - ride.s ) ? B : 0;
	}

	/** Get how long it would take for the car to give the ride, including possibly waiting. */
	static int getTotalCarJourneyTime ( CarInfo car, RideInfo ride )
	{
		return Math.max( distToStart( car, ride ), ride.s - car.t ) + ride.dist;
	}

	/** Gets whether the journey cannot be performed in time. */
	private static boolean isJourneyInfeasible ( CarInfo car, RideInfo ride )
	{
		return car.t + getTotalCarJourneyTime( car, ride ) > ride.f;
	}

	/** Gets distance of car to start of ride. */
	private static int distToStart ( CarInfo car, RideInfo ride )
	{
		return manhattenDist( car.x, car.y, ride.a, ride.b );
	}

	/** Gets Manhatten distance. */
	private static int manhattenDist ( int x1, int y1, int x2, int y2 )
	{
		return Math.abs( x1 - x2 ) + Math.abs( y1 - y2 );
	}

	/** Get input from file. */
	private static void getInput ( Files file )
	{
		int[] inputIntArray; /* Used to get and assign input. */

		try ( BufferedReader br = new BufferedReader( new FileReader( file.getInputPath().toFile() ) ) )
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

			inputIntArray = nextLineAsIntArray.get();

			R = inputIntArray[ 0 ];
			C = inputIntArray[ 1 ];
			F = inputIntArray[ 2 ];
			N = inputIntArray[ 3 ];
			B = inputIntArray[ 4 ];
			T = inputIntArray[ 5 ];

			rides = new ArrayList<RideInfo>( N );

			for ( int i = 0; i < N; i++ )
			{
				rides.add( new RideInfo( i, nextLineAsIntArray.get() ) );
			}

			/* Sort in order of soonest start of ride to latest. */
			Collections.sort( rides, new Comparator<RideInfo>()
			{
				@Override
				public int compare ( RideInfo r1, RideInfo r2 )
				{
					return r1.s - r2.s;
				}
			}.reversed() );

			cars = new CarInfo[ F ];

			for ( int i = 0; i < F; i++ )
			{
				cars[ i ] = new CarInfo();
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	/** Output solution to file with filename given. */
	private static void output ( Files file, List<List<Integer>> solution )
	{
		try ( BufferedWriter bw = new BufferedWriter( new FileWriter( file.getOutputPath().toFile() ) ) )
		{
			for ( List<Integer> vehicleInfo : solution )
			{
				bw.write( Integer.toString( vehicleInfo.size() ) );
				for ( int rideNum : vehicleInfo )
				{
					bw.write( " " + Integer.toString( rideNum ) );
				}

				if ( vehicleInfo == solution.get( solution.size() - 1 ) )
				{
					break;
				}
				bw.write( '\n' );
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	private enum Files
	{
		A ( "a_example" ),
		B ( "b_should_be_easy" ),
		C ( "c_no_hurry" ),
		D ( "d_metropolis" ),
		E ( "e_high_bonus" );

		public String str;

		Files( String str )
		{
			this.str = str;
		}

		Path getInputPath ()
		{
			return Paths.get( "input", "hashcode2018", str + ".in" );
		}

		Path getOutputPath ()
		{
			return Paths.get( "output", "hashcode2018", str + ".output" );
		}
	}

	private static class RideInfo
	{
		final int rideID;

		int	a;	/* The row of the start intersection. */
		int	b;	/* The column of the start intersection. */
		int	x;	/* The row of the finish intersection. */
		int	y;	/* The column of the finish intersection. */
		int	s;	/* The earliest start time. */
		int	f;	/* The latest finish time. (in [0,T], f >= s + |x-a| + |y-b|) */

		int dist; /* Distance from start to end. */

		public RideInfo( int rideID, int a, int b, int x, int y, int s, int f )
		{
			this.rideID = rideID;
			this.a = a;
			this.b = b;
			this.x = x;
			this.y = y;
			this.s = s;
			this.f = f;

			dist = manhattenDist( a, b, x, y );
		}

		public RideInfo( int rideID, int[] in )
		{
			this( rideID, in[ 0 ], in[ 1 ], in[ 2 ], in[ 3 ], in[ 4 ], in[ 5 ] );
		}
	}

	private static class CarInfo
	{
		int	x;	/* The row of the car. */
		int	y;	/* The column of the car. */
		int	t;	/* Time from which car is available. */

		public CarInfo( int x, int y, int t )
		{
			this.x = x;
			this.y = y;
			this.t = t;
		}

		public CarInfo()
		{
			this( 0, 0, 0 );
		}
	}
}
