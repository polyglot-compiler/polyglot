#!/usr/local/bin/perl -w

$outputting = 0;
$last_was_blank = 0;

while(<>) {
    if (/^===+\s+(\S+)/) {
	close(OUT) if ($outputting);
	open(OUT, ">$1.java");
	$outputting = 1;
    }
    if (/^\s*$/) {
	if (! $last_was_blank) {
	    $last_was_blank = 1;
	    print OUT "\n" if ($outputting);
	}
    } else {
	$last_was_blank = 0;
	print OUT $_ if ($outputting);
    }
}

    
	    
    
