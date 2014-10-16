/**
 * Interface for a List containing a list of elements A list is an ordered
 * collection of elements Every element has an index
 * 
 * @author karthik
 * @param <E>
 */
interface MyList {
    
    /**
     * Add an element into myList
     */
    void add(Object e);
    
    String jlc$CompilerVersion$jl = "2.6.1";
    long jlc$SourceLastModified$jl = 1413431332000L;
    String jlc$ClassType$jl = ("H4sIAAAAAAAAALVXa2wUVRS+O90+KfSBNMijRagYXrti1IRUDVALLGxlbTFK" +
                               "iax3Z+7uDp2dO8zcbbcgBlADEa0JlpeKJgYVCL5QYowh4ZdC8I/GmPjDxz9/" +
                               "KD/45R8Vz7mzu7MdloXyaDJn75xz7j3nu/ec705PXSLVjk3mWNwYSRlchMSI" +
                               "xZxQjNoO07oN6jgbQBFXDywKjx3a3Hy6ijQNkCbd7BdU6Go3NwXLiQHSmGGZ" +
                               "BLOdFZrGtAHSYjKm9TNbp4a+DRy5OUBaHT1lUpG1mdPHHG4MoWOrk7WYLWMW" +
                               "lFHSqHLTEXZWFdx2BGmObqFDNJwVuhGO6o7oipKapM4MzdlKXiCBKKlOGjQF" +
                               "jm3RAoqwXDG8CvXg3qBDmnaSqqwwJTiom5ogHf4ZRcSd68ABptZmmEjzYqig" +
                               "SUFBWt2UDGqmwv3C1s0UuFbzLEQRZMY1FwWnOouqgzTF4oJM9/vFXBN41ctt" +
                               "wSmCTPO7yZVyNpnhO7OS07r0xCOj2801piJz1phqYP7VMKndN6mPJZnNTJW5" +
                               "ExsXRg/StrN7FULAeZrP2fX58vnLyxe3nzvv+sws47M+sYWpIq4eS0z5flb3" +
                               "gmVVmEadxR0dS2EccnmqsbylK2dBLbYVV0RjqGA81/fNxp0n2Z8KaYiQGpUb" +
                               "2QxUVYvKM5ZuMHs1M5lNBdMipJ6ZWre0R0gtjKO6yVzt+mTSYSJCgoZU1XD5" +
                               "DluUhCVwi4Iw1s0kL4wtKtJynLNI/i8Az+T8bx0qBHkgnOYZFh6ktkjrg+He" +
                               "HjO1JGZz3IRwEWnvyArbpiNYwaEthnVTs3KYy+ThQAC2aZa/ZQ2o7zXc0Jgd" +
                               "V8eyK3sufxy/qBSLNo9CkJpeuRoJBOQyd2EluzsN+zQI/QfGxgX9z659bu/c" +
                               "KjhiaziIKHOyBaYVXmCiL75stlVfnTty5s1Fy5TSvmwqaeB+JtxTbvHibrAZ" +
                               "A/0vh2NvHLi0Z5MMCh7zygXoRNkNZ07hsLn98vmtP//267EflWKiiiB1NAH0" +
                               "QVUhSH2x86/Kf/a12ke2/rHdY+9o699f6hZ56/iS7DGzmY9++ve70OHfL5TZ" +
                               "4XrBrSUGG2JGScwGCNnhC9kruSUCZEehA+Pqid5TF1bPV/crpCrftmUoavyk" +
                               "rtLgwHQ2A4Y1EQZqaiH3uf46sbnKNKBhL+7COfRM/OyOTgWXqAf2FRT6A6is" +
                               "3R98HF10FY4YQylRMinJ7Qw10FTgywaRtvmwp5EFPEmOp+R7iEyFpzH/gr8E" +
                               "rc0Wyha34KX/dClnomgvnHSNlU0Yuoq6DjzT+V5RAQEY0ElAHE7nU2aGa3pS" +
                               "pwmDYXn/03Tv0jN/jTa7Z2eAxk3OJouvv4Cnv3sl2Xlx89/tcpmAiheQV+ie" +
                               "m1vvU72Vix2d2/XD7CPf0qPAj8BJjr6NSZoJSHgBibcV7j85E++akEusclMe" +
                               "kub7pXwQdy1f2fi+DMUiQaqopjlXc37M1jNAqUN5zmd7x165EhodU0ouxnlX" +
                               "3U2lc9zLUYZqcM/yCvwF4PkPH8wDFS45tnbnGXpOkaItCw/rnkppyRCr/vhk" +
                               "x9fHd+xR8rjmCRIc4rp7G9/nRSblI7d5l7S7cSEVexyiV6iqKhyGZUVBfcEH" +
                               "CySUq7DXayvYom6iKB6XiocrxRTQTbpJ5VW9RrpHUKxD0QuslmJCclShLpq8" +
                               "upB6VD+Jos8f9vpQ3ZgVoDxTwTZwgzCVYjwX3tMoNqLYBPDS1El3c80lii4U" +
                               "j7kJLYdCBhrH4WYU8RuE54Xr8CPLd5jMVS6RrAAvfYPw5HJh6SHJN4VChyJi" +
                               "W7PUcMrhqk1wbjBq4qs898ytYytNXVSwDU0cljRlUQzDgQnufvqW4SnXgPpt" +
                               "KLbfgYLcVcH24oT6DkXMq8qdKHajeAkJgAs9OYJvj6LYI0d3AM6rFWyjtwJn" +
                               "H4rXULwOXyYunBWGUUS0/3YiKr25fKUeNLiZkmsfqjAJyqjdK6MIfrzZWQuu" +
                               "jJ6cyiy8TeUSb9/KjhxE8RaKdyGtYaqL4ma8N5HNqNCDiudwGEVcrvPhtVno" +
                               "qHQ4MWEijUm3D1AcR3HSQ3E78ZRW42fXQ/H5TaL4FMVpFF+UQ5Er/utiwQ3+" +
                               "P0T9Moy4EAAA");
}

/**
 * A list implementation using an expanding array as the underlying data
 * structure
 * 
 * @author karthik
 */
public class MyArrayList implements MyList {
    /**
     * Current size of MyArrayList
     */
    private int size = 0;
    
    /**
     * Initialise MyArrayList
     */
    MyArrayList() { super(); }
    
    /**
     * test meth
     */
    void sub() {  }
    
    /**
     * Adds the input element into the list
     */
    public void add(Object e) {  }
    
    public static final String jlc$CompilerVersion$jl = "2.6.1";
    public static final long jlc$SourceLastModified$jl = 1413431332000L;
    public static final String jlc$ClassType$jl = ("H4sIAAAAAAAAAJ1WXWwUVRQ+O/3/gf5oGynQ1nYhQmE38GKwaIANheLW1hab" +
                                                   "sAbW25m726Gzc4c7d8sWrUETA/GBBy1/Rn3CREmVxIQYY0h4USH4ojEmPvjz" +
                                                   "pgnywIP6ouK5d2Z3dqc/Jja5t3fP3z33nO+cM4v3oMbl0Osway5rMRETcw51" +
                                                   "Y2OEu9RIWMR1DyMhrZ8fiC9cPNb6cRW0pKDFtCcEEaaeYLagBZGC5hzNTVHu" +
                                                   "7jUMaqSgzabUmKDcJJZ5CgWZnYJ218zaROQ5dcepy6xZKdju5h3K1Z1FYhKa" +
                                                   "dWa7gud1wbgroDV5nMySeF6YVjxpumIwCbUZk1qGewJehkgSajIWyaJgZ7L4" +
                                                   "iriyGB+SdBRvNNFNniE6LapUz5i2IaAnrFF6cfRpFEDVuhwV06x0VbVNkADt" +
                                                   "nksWsbPxCcFNO4uiNSyPtwjoWtEoCtU7RJ8hWZoW8EhYbsxjoVSDCotUEdAR" +
                                                   "FlOWChy6Qjkry9a9Z3afe9E+aGvKZ4PqlvS/BpW6Q0rjNEM5tXXqKTZvTV4g" +
                                                   "nTfOagAo3BES9mQ+een+nm3dN295MuuXkRmdOk51kdavTK39ekNiy64q6Ua9" +
                                                   "w1xTQqHi5SqrYz5nsOAgFjtLFiUzVmTeHP/iyOmr9K4GjcNQqzMrn0NUteks" +
                                                   "55gW5QeoTTkR1BiGBmobCcUfhjo8J02betTRTMalYhiqLUWqZeo3hiiDJmSI" +
                                                   "qvFs2hlWPDtETKtzwQGAOlwQwRX3/3dIgoCd8WmWo/EZwsW0ORMf2W9nt49x" +
                                                   "JoMQL710ZG4v52ROIjh23HL+l1ZB+rLmZCSCYdoQLlkL8X2QWQblaX0hv2//" +
                                                   "/Y/Sd7QSaP1XCGgqMwmRiLL1sISzF24M1gwWITKbt0wcPfTC2b4qzLNzslo+" +
                                                   "GUWjYdQFtTqMJ4JQSustZ37949qFeRbgT0B0SVks1ZSw7gu/izOdGtg2AvNb" +
                                                   "e8n19I35qCZz1IDdQhDMJ5Zed/iOCngPFluFjIWWhKYM4zliSVaxvhvFNGcn" +
                                                   "A4oKeJM6r/VzDoh76PV/9EiC5LY6cm/zEiQjGnqFakVDn968fP2tgV1aeddq" +
                                                   "KWtvE1R4NdAWJOQwpxTpP1wae/P8vTPPq2ygRP9yF0TlnsCKIFgKjL9268T3" +
                                                   "P/145VutlEEooOrmwDiWiYV4w/Jyo8/ZOWaYGZNMWVTm/6+WTTuu/3au1Uui" +
                                                   "hRQvJBy2/beBgL5uH5y+c+zPbmUmoss2HTw4EPPe/VBguQTSwivfbLz8JXkH" +
                                                   "uwhWrmueoqoYwX+QdGqLylBU7Y+FeANyW19QvI4SjMN9cEgOlABhqfji212J" +
                                                   "p+4qpwOESRtdhaW1N0nKwL/zau53ra/2cw3qUtCqZhmxxSSx8jKPKZxGbsIn" +
                                                   "JmFNBb9ysnhtdLBUQRvC6C67NoztoObxLKXluS4E5wYZpE5cXT6cu8JwjoA6" +
                                                   "7FAqG9XeK7d+L5AC6hxuzhL5EeClZpnQjnEzhx181h8x9OzC6w9i5xa0sjnc" +
                                                   "v2QUlut4s1j53uj5/gD/Irj+kUv6LAleL25P+AOhtzQRHEei/tHV3FJXDP1y" +
                                                   "bf6z9+fPeIOtvXIM7bfzuQ+/+/ur2KWfby/TVavwE0PVfmHFUNU6+SnL1CuR" +
                                                   "KM/tyBvxoI5+blxprCsfr7y68K4x+t4OzUf3kwIaBHO2W3SWWmWWNbTUE7I0" +
                                                   "oj5lAqB+MLJ4+8Bm/Q0NqkoYW/JFVKk0WImsRk7xg84+XIGvdZXtsgnXJv/H" +
                                                   "pmXbpdx6Vqng0VV4z8otifF381NK4HG5PeEFeTeCcpaZ3tfboZJb9eBDfcB3" +
                                                   "a2BFt/aEro5Upq1VtStZrDGvWJXmkVX8PSq3SfSXGIY8ThQqJ7KjPEgV/gVr" +
                                                   "Ftc6mAsAAA==");
}
