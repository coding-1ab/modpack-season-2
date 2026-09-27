use std::process::Command;
use std::fs;

fn main() {
    let contents = fs::read_to_string("upstreams").unwrap();
    for (i, line) in contents.lines().enumerate() {
        let split: Vec<&str> = line.split(" ").collect();
        let [name, url, ..] = split[..] else {
            panic!("invalid upstreams files. Check line number {}", i + 1);
        };
        assert!(url.ends_with(".git"), "invalid url detected: {}", url);

        let mut command = Command::new("josh");
        let args = ["fetch", "-r", name];
        command.args(args);
        let joined = {
            let mut joined = String::with_capacity(32);
            for arg in args {
                joined.push_str(arg);
                joined.push_str(" ");
            }
            joined
        };

        println!("+josh {joined}");
        let output = command.output().expect("failed to execute process");
        let output = String::from_utf8(output.stdout).unwrap();
        println!("{}", output);
    }
}
