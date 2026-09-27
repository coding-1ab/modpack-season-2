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
        assert_valid_remote(url);

        let filter = format!(":prefix=mods/{}", name);
        let args = ["remote", "add", name, url, &filter];
        let output = run_command("josh", &args);
        println!("{}", output);
    }

    let origin_remote = run_command("git", &["remote", "get-url", "origin"]);
    println!("{}", run_command("josh", &["remote", "add", "origin", &origin_remote]));
}

fn assert_valid_remote(url: &str) {
    let result = run_command("curl", &["-L", "-s", "-o", "/dev/null", "-w", "%{http_code}", url]);
    assert_ne!(&result, "404", "Remote {} is invalid!", url);
}

fn run_command(base: &str, args: &[&str]) -> String {
    let mut command = Command::new(base);
    command.args(args);
    let joined = {
        let mut joined = String::new();
        for arg in args {
            joined.push_str(arg);
            joined.push_str(" ");
        }
        joined
    };

    println!("+{base} {joined}");
    let output = command.output().expect("failed to execute process");
    let output = String::from_utf8(output.stdout).unwrap();
    output
}
