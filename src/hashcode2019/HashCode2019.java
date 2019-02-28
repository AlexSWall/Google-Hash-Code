package hashcode2019;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Alex
 * @Team Myself, Charles Clarke, Andrew Dunnings, and Oliver Masters.
 */
public class HashCode2019
{
	/* File to deal with —— { A = a_example, B = b_should_be_easy, C = c_no_hurry, D = d_metropolis, E = e_high_bonus }. */
	private static final HashcodeFile file = HashcodeFile.A;

	public static void main ( String[] args )
	{
		getInput( file );
		List<List<Integer>> solution = getAssignment();
		output( file, solution );
	}

	/**
	 * Gets the assignment of cars, indexed by ints, to the list of rides it will give, also index by ints.
	 *
	 * @param scoreFunc The function that calculates the score of a ride for a car — used when greedily trying to assign rides to cars.
	 * @return A list which has, indexed by the integer value of the corresponding car, the rides ids the car has been allocated.
	 */
	private static List<List<Integer>> getAssignment ()
	{
		return null;
	}

	/** Get input from file. */
	private static void getInput ( HashcodeFile file )
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

			/* Input here */

		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	/** Output solution to file with filename given. */
	private static void output ( HashcodeFile file, List<List<Integer>> solution )
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

	private enum HashcodeFile
	{
		A ( "" ),
		B ( "" ),
		C ( "" ),
		D ( "" ),
		E ( "" );

		public String str;

		HashcodeFile( String str )
		{
			this.str = str;
		}

		Path getInputPath ()
		{
			return Paths.get( "input", "hashcode2019", str + ".in" );
		}

		Path getOutputPath ()
		{
			return Paths.get( "output", "hashcode2019", str + ".output" );
		}
	}
}
